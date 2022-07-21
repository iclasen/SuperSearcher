CREATE TABLE search_term (
    term_id                 NUMBER NOT NULL,
    search_id               NUMBER,
    term                    VARCHAR2(100),
    is_regex                VARCHAR2(5),
    ignores_case            VARCHAR2(5));

ALTER TABLE search_term ADD CONSTRAINT pk_search_term PRIMARY KEY (term_id);
CREATE SEQUENCE sq_search_term AS INT START WITH 0 INCREMENT BY 1;


CREATE TABLE file (
    file_id                 NUMBER NOT NULL,
    file_name               VARCHAR2(260),
    -- Current windows standards limits path lengths to 260 characters
    -- extending this to help future proof at least a little
    file_path               VARCHAR2(1000) NOT NULL);

ALTER TABLE file ADD CONSTRAINT pk_file PRIMARY KEY (file_id);
ALTER TABLE file ADD CONSTRAINT uc_file UNIQUE (file_path);
CREATE SEQUENCE sq_file AS INT START WITH 0 INCREMENT BY 1;

CREATE TABLE file_line (
    line_id                 NUMBER NOT NULL,
    file_id                 NUMBER NOT NULL,
    line_text               VARCHAR2(1000),
    line_number             NUMBER);

ALTER TABLE file_line ADD CONSTRAINT pk_file_line PRIMARY KEY (line_id);
ALTER TABLE file_line ADD CONSTRAINT fk_file_line_id FOREIGN KEY (file_id) REFERENCES file(file_id);
CREATE SEQUENCE sq_file_line AS INT START WITH 0 INCREMENT BY 1;

CREATE TABLE result (
    result_id               NUMBER NOT NULL,
    term_id                 NUMBER NOT NULL,
    file_id                 NUMBER NOT NULL,
    line_id                 NUMBER);

ALTER TABLE result ADD CONSTRAINT pk_result PRIMARY KEY (result_id  );
CREATE SEQUENCE sq_result AS INT START WITH 0 INCREMENT BY 1;

