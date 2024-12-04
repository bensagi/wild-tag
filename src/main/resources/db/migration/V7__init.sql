ALTER TABLE images
ADD COLUMN folder_name TEXT;

ALTER TABLE images
ADD COLUMN jbg_name TEXT;

ALTER TABLE images
ADD COLUMN jbg_date TEXT;

ALTER TABLE images
ADD COLUMN jbg_time TEXT;

ALTER TABLE images
ADD CONSTRAINT unique_jbg_name UNIQUE (jbg_name);

