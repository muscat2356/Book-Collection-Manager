-- ----------------------------------------
-- 1. 出版社カラム追加 → テストデータ埋め → NOT NULL
-- ----------------------------------------
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS publisher VARCHAR(255);

-- V2 シードの書誌（title で特定）に出版社を付与
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'リーダブルコード';
UPDATE books SET publisher = 'アスキー' WHERE title = '達人プログラマー';
UPDATE books SET publisher = 'Pearson' WHERE title = 'Clean Code';
UPDATE books SET publisher = 'Pearson' WHERE title = 'Clean Architecture';
UPDATE books SET publisher = 'ピアソン・エデュケーション' WHERE title = 'リファクタリング';
UPDATE books SET publisher = '翔泳社' WHERE title = 'エリック・エヴァンスのドメイン駆動設計';
UPDATE books SET publisher = 'オーム社' WHERE title = 'テスト駆動開発';
UPDATE books SET publisher = 'ピアソン・エデュケーション' WHERE title = 'Effective Java';
UPDATE books SET publisher = 'ソフトバンククリエイティブ' WHERE title = 'Java言語で学ぶデザインパターン入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'プログラミング言語Go';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '入門Python3';
UPDATE books SET publisher = '翔泳社' WHERE title = '独習Java';
UPDATE books SET publisher = 'インプレス' WHERE title = 'スッキリわかるJava入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Spring徹底入門';
UPDATE books SET publisher = '秀和システム' WHERE title = 'Spring Boot 3 プログラミング入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'SQLアンチパターン';
UPDATE books SET publisher = '翔泳社' WHERE title = '達人に学ぶDB設計徹底指南書';
UPDATE books SET publisher = '翔泳社' WHERE title = 'SQL第2版 ゼロからはじめるデータベース操作';
UPDATE books SET publisher = '技術評論社' WHERE title = 'Webを支える技術';
UPDATE books SET publisher = 'オーム社' WHERE title = 'マスタリングTCP/IP入門編';
UPDATE books SET publisher = '日経BP' WHERE title = 'ネットワークはなぜつながるのか';
UPDATE books SET publisher = '技術評論社' WHERE title = 'プロになるためのWeb技術入門';
UPDATE books SET publisher = 'ソフトバンククリエイティブ' WHERE title = '体系的に学ぶ安全なWebアプリケーションの作り方';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Docker/Kubernetes実践コンテナ開発入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Kubernetes完全ガイド';
UPDATE books SET publisher = '翔泳社' WHERE title = 'インフラエンジニアの教科書';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '入門 監視';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'SREサイトリライアビリティエンジニアリング';
UPDATE books SET publisher = 'LPI-Japan' WHERE title = 'Linux標準教科書';
UPDATE books SET publisher = 'SBクリエイティブ' WHERE title = '新しいLinuxの教科書';
UPDATE books SET publisher = '技術評論社' WHERE title = 'プログラマのためのGit';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '実用Git';
UPDATE books SET publisher = 'オーム社' WHERE title = 'アジャイルサムライ';
UPDATE books SET publisher = '翔泳社' WHERE title = 'カイゼン・ジャーニー';
UPDATE books SET publisher = '技術評論社' WHERE title = 'チーム開発実践入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'オブジェクト指向設計実践ガイド';
UPDATE books SET publisher = '秀和システム' WHERE title = 'プリンシプル オブ プログラミング';
UPDATE books SET publisher = '技術評論社' WHERE title = '良いコード/悪いコードで学ぶ設計入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'ドメイン駆動設計入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'マイクロサービスアーキテクチャ';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'データ指向アプリケーションデザイン';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'コンピュータシステムの理論と実装';
UPDATE books SET publisher = '共立出版' WHERE title = 'プログラミングのための線形代数';
UPDATE books SET publisher = '翔泳社' WHERE title = 'アルゴリズム図鑑';
UPDATE books SET publisher = '講談社' WHERE title = '問題解決力を鍛える アルゴリズムとデータ構造';
UPDATE books SET publisher = '翔泳社' WHERE title = 'なっとく!アルゴリズム';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'ゼロから作るDeep Learning';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'Pythonではじめる機械学習';
UPDATE books SET publisher = '技術評論社' WHERE title = '達人が教えるWebパフォーマンスチューニング';
UPDATE books SET publisher = 'オーム社' WHERE title = 'ハッカーと画家';

-- 万一未設定が残っていた場合のフォールバック（NOT NULL 化前の安全策）
UPDATE books SET publisher = '不明' WHERE publisher IS NULL;

ALTER TABLE books
  ALTER COLUMN publisher SET NOT NULL;
