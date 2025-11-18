# nishibi-bbs

bbs.misononoa.ccで使いたくて作っています。
<br>
いろいろとレベルが低いところが多いかと思いますが、いずれどこへでも持っていける形にしたい。

## 構成

- PostgreSQL 16 
    : DB。17にしたいです。そのうち。
- Spring Data JPA + JPA Hibernate
    : ORM。
- Spring Web MVC
    : MVC。
    - Thymeleaf
        : テンプレートエンジン。
    - HTMX Spring Boot Thymeleaf
        : かんたん便利なHTMX統合。
    - CommonMark + AutoLink Extension
        : Markdownレンダリングエンジン。
- HTMX
    : かんたん便利。

## 構築

まだ。

## 設定

まだ。
一応`application-prod.yaml`を作ってclasspathに指定すればいろいろできると思います。試してません。

## クレジット

### KHドットフォント

表示フォントに KHドットフォント秋葉原16(丸ゴシック)を使用しています。
Licensed under SIL Open Font License 1.1
© Keitarou Hiraki, Font Silo 1990-2015.

### M PLUS Code

コードブロック表示フォントに M PLUS Code を使用しています。
Copyright 2021 The M+ FONTS Project Authors (https://github.com/coz-m/MPLUS_FONTS) This Font Software is licensed under the SIL Open Font License, Version 1.1. This license is copied below, and is also available with a FAQ at: https://scripts.sil.org/OFL

### Spring Framework, Spring Boot by VMWare Tanzu

アプリケーションフレームワークにSpring Frameworkを使用しています。
The Spring Framework is released under version 2.0 of the Apache License.
Copyright © 2005 - 2025 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.