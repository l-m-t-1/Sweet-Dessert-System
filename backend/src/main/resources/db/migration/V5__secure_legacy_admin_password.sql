UPDATE `user`
SET password = '$2b$10$h3FHNf8rsh7X8YjB29KbdOmQesaV8QbMyuqwmZX.5qroyEH/22Bv2',
    role = 'ADMIN',
    status = 1
WHERE username = 'admin'
  AND password NOT LIKE '$2%';
