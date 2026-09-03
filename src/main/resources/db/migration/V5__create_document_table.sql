CREATE TABLE document (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL,
    source_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    embedding vector(768) NOT NULL
);
