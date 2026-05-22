INSERT INTO sites (id, name, city, opening_time, closing_time, active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'The Atomium Padel Club', 'Bruxelles', '08:00:00', '23:00:00', true),
    ('22222222-2222-2222-2222-222222222222', 'The Carré Club', 'Liège', '08:00:00', '23:00:00', true),
    ('33333333-3333-3333-3333-333333333333', 'Arlon Green Padel', 'Arlon', '09:00:00', '20:00:00', true);

INSERT INTO courts (id, name, type, site_id)
VALUES
    (RANDOM_UUID(), 'Court1', 'Indoor', '11111111-1111-1111-1111-111111111111'),
    (RANDOM_UUID(), 'Court2', 'Outdoor', '11111111-1111-1111-1111-111111111111'),

    (RANDOM_UUID(), 'Court1', 'Outdoor', '22222222-2222-2222-2222-222222222222'),
    (RANDOM_UUID(), 'Court2', 'Indoor', '22222222-2222-2222-2222-222222222222'),

    (RANDOM_UUID(), 'Court1', 'Indoor', '33333333-3333-3333-3333-333333333333'),
    (RANDOM_UUID(), 'Court2', 'Outdoor', '33333333-3333-3333-3333-333333333333');
INSERT INTO members (id, matricule, first_name, last_name, type, site_id)
VALUES
    ('44444444-4444-4444-4444-444444444444', 'G1234', 'Vincent', 'Harmegnies', 'GLOBAL', null),
    ('55555555-5555-5555-5555-555555555555', 'S5678', 'Sophie', 'Dupont', 'GLOBAL', null),
    ('66666666-6666-6666-6666-666666666666', 'L9012', 'Jean', 'Martin', 'GLOBAL', null);
