# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 応答言語

ユーザーとのやり取りは日本語で行うこと。

## プロジェクト概要

nishibi-bbsは`bbs.misononoa.cc`用のシンプルなシングルスレッド掲示板Webアプリケーションです。Spring Boot（Java 25ツールチェーン、Kotlinは`buildSrc`でのみ使用）+ PostgreSQL 18、Spring Data JPA/Hibernate、Thymeleafを使ったSpring Web MVC、HTMX（`htmx-spring-boot-thymeleaf`経由）、投稿レンダリング用のCommonMark（+ AutoLink拡張）で構成されています。

## よく使うコマンド

- ビルド: `./gradlew build`
- アプリ実行: `./gradlew bootRun`
- 全テスト実行: `./gradlew test`
- 単一テストクラスの実行: `./gradlew test --tests "cc.misononoa.nishibi.core.web.view.TemplateHtmlTest"`
- 単一テストメソッドの実行: `./gradlew test --tests "cc.misononoa.nishibi.core.web.view.TemplateHtmlTest.isWellFormedMarkup"`

Gradleタスク（`bootRun`と`test`）は、`buildSrc/src/main/kotlin/DotEnv.kt`のカスタム`DotEnv`ローダーを通じて、リポジトリルートの`.env`ファイルから環境変数を自動的に読み込みます。DB設定はすべて環境変数（`NISHIBI_DB_HOST`、`NISHIBI_DB_PORT`、`NISHIBI_DB_NAME`、`NISHIBI_DB_USERNAME`、`NISHIBI_DB_PASSWORD`）経由で行われ、`src/main/resources/application.yaml`で参照されています。リポジトリ内に`application-prod.yaml`は存在せず（gitignore対象）、デフォルトでは`application-dev.yaml`がアクティブプロファイルです（`spring.profiles.active: dev`）。

## アーキテクチャ

`cc.misononoa.nishibi`パッケージ配下の単一モジュールSpring Bootアプリです。

- `controller/` — MVCコントローラー。`PostController`は同じエンドポイントに対してフルページ描画とHTMX部分描画の両方を扱っており、`@HxRequest`の有無によるメソッドの使い分けと、Spring 4の`FragmentsRendering`（ビュー全体ではなく`index::post-item`のような特定のThymeleafフラグメントを返す）で実現しています。`POST /post`のバリデーションエラーは`@ExceptionHandler(BindException.class)`で捕捉し、`@HxRetarget`を使って`postform-wrap`フラグメントとして再描画します。
- `service/PostService` — ビジネスロジック。投稿作成、ページング（`getPosts`）、ハッシュによる検索（`getByHash`/`get`）を担当します。
- `logic/PostHashLogic` — Spring非依存の純粋なロジック。投稿内容・リモートアドレス・タイムスタンプからSHA-1の投稿ハッシュを生成します。また、投稿内容中の返信・引用リンク（`#<7〜40桁の16進数>`）を検出する正規表現を提供しており、`PostRelation`の保存時と、`NishibiMdProcessor`でのリンク描画時の両方で使われています。
- `model/entity/` — JPAエンティティ（`Post`、`PostRelation`）。`Post.abbrevHash`はHibernateの`@Formula`（`substring(post_hash for 7)`）でSQL側で計算されるため直接設定できず、`PostRepository.findByAbbrevHash`で検索します。投稿同士は`PostRelation`（自己参照の多対一ペア`post`/`relatedPost`）で関連付けられ、保存の都度`PostService.savePostRelation`で作り直されます。
- `core/web/view/NishibiMdProcessor` — カスタムThymeleaf属性プロセッサ（`nishibi:render`、`core/web/WebConfig`でダイアレクトとして登録）。投稿内容をMarkdown（CommonMark + autolink）としてレンダリングした後、結果のHTMLを後処理して`#<hash>`形式の参照を`/post/{abbrevHash}`へのリンクに変換します。HTMLエスケープはCommonMarkレンダラー側で行われ、リンクマークアップ生成時にハッシュ値もさらにHTMLエスケープされます。
- `core/web/filter/CspNonceFilter` — リクエストごとにCSPのnonceを発行し、`SecurityConfig`のCSPヘッダー（`script-src 'nonce-{nonce}' 'strict-dynamic'`）に組み込まれます。
- `core/security/SecurityConfig` — 認証・セッション機構（Basic認証、フォームログイン、ログアウト、Remember-me）はすべて明示的に無効化されており、アプリにログイン機能はありません。CORSのオリジンは`nishibi.web.cors.allowed-origins`で設定可能です。
- `core/web/interceptor/RateLimitInterceptor` + `WebConfig.RateLimitProperties`（`nishibi.web.rate-limit.window`/`.limit`） — リクエストのレート制限を行うMVCインターセプターとして登録されています。
- `core/util/TimeUtils` / `SqlTimeUtils` — ハッシュ生成やDBタイムスタンプ処理で使われる時刻ユーティリティです。
- テンプレートは`src/main/resources/templates/`にあります。`layout.html`/`common.html`は`thymeleaf-layout-dialect`（`nz.net.ultraq.thymeleaf`）で共有されています。`index.html`はHTMX部分更新のためにコントローラーが個別に返すフラグメント（`post-item`、`postform-wrap`、`pager`）を定義しています。

## テストに関する注意

- `TemplateHtmlTest`（`src/test/java/.../core/web/view/`配下）は`classpath:/templates/**/*.html`以下の全テンプレートをattoparserの厳格HTMLモードでパースし、不正なマークアップがあれば失敗します。テンプレートを編集した後は必ず実行してください。
- 各レイヤーには標準のSpring Bootテストスターター（data-jpa、jdbc、thymeleaf、webmvc、security、actuator向けの`*-test`）が使われています。
