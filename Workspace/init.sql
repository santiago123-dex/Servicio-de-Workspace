
CREATE TABLE workspace (
    id SERIAL PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status SMALLINT NOT NULL,
    data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workspace_member (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    workspace_id INT NOT NULL,
    role SMALLINT NOT NULL,
    CONSTRAINT fk_workspace_member_workspace 
        FOREIGN KEY (workspace_id) 
        REFERENCES workspace(id) 
        ON DELETE CASCADE
);

CREATE TABLE assignment (
    id SERIAL PRIMARY KEY,
    workspace_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    due_date TIMESTAMPTZ NOT NULL,
    status SMALLINT NOT NULL,
    rubric JSONB,
    settings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_workspace 
        FOREIGN KEY (workspace_id) 
        REFERENCES workspace(id) 
        ON DELETE CASCADE
);

CREATE TABLE submission (
    id SERIAL PRIMARY KEY,
    assignment_id INT NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content JSONB,
    files JSONB,
    result JSONB,
    CONSTRAINT fk_submission_assignment 
        FOREIGN KEY (assignment_id) 
        REFERENCES assignment(id) 
        ON DELETE CASCADE
);
