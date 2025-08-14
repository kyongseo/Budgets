-- roles
INSERT INTO roles(name)
SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='USER');

INSERT INTO roles(name)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='ADMIN');

-- admin user
INSERT INTO users(username, password)
SELECT 'admin@domain.com', '{noop}admin'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin@domain.com');

-- user_roles (ADMIN 부여)
INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM (SELECT id FROM users WHERE username='admin@domain.com') u,
     (SELECT id FROM roles  WHERE name='ADMIN') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id=u.id AND role_id=r.id
);

-- budget_category
INSERT INTO budget_category(name) SELECT '식비'        WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='식비');
INSERT INTO budget_category(name) SELECT '카페/간식'   WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='카페/간식');
INSERT INTO budget_category(name) SELECT '교통비'      WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='교통비');
INSERT INTO budget_category(name) SELECT '주거/관리비' WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='주거/관리비');
INSERT INTO budget_category(name) SELECT '통신비'      WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='통신비');
INSERT INTO budget_category(name) SELECT '쇼핑/의류'   WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='쇼핑/의류');
INSERT INTO budget_category(name) SELECT '의료/건강'   WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='의료/건강');
INSERT INTO budget_category(name) SELECT '교육/자기계발' WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='교육/자기계발');
INSERT INTO budget_category(name) SELECT '문화/여가'   WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='문화/여가');
INSERT INTO budget_category(name) SELECT '구독/멤버십' WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='구독/멤버십');
INSERT INTO budget_category(name) SELECT '경조사/선물' WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='경조사/선물');
INSERT INTO budget_category(name) SELECT '보험'        WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='보험');
INSERT INTO budget_category(name) SELECT '세금/공과금' WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='세금/공과금');
INSERT INTO budget_category(name) SELECT '저축/투자'   WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='저축/투자');
INSERT INTO budget_category(name) SELECT '반려동물'    WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='반려동물');
INSERT INTO budget_category(name) SELECT '기타'        WHERE NOT EXISTS (SELECT 1 FROM budget_category WHERE name='기타');
