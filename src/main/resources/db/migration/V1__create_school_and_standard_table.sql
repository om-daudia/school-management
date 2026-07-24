--school table
CREATE TABLE school(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    school_name VARCHAR(100) NOT NULL
);

--standard table
CREATE TABLE standard (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    standard INT NOT NULL,
    school_id INT,
    CONSTRAINT fk_school
        FOREIGN KEY (school_id)
        REFERENCES school(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);