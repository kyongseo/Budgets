INSERT INTO roles (name)
SELECT 'USER'
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO roles (name)
SELECT 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO users (username, password)
SELECT 'admin@domain.com', 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin@domain.com');

INSERT INTO user_roles (user_id, role_id)
SELECT
    (SELECT id FROM users WHERE username = 'admin@domain.com'),
    (SELECT id FROM roles WHERE name = 'ADMIN')
    WHERE NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin@domain.com')
    AND role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
);