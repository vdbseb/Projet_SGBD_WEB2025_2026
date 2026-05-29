BEGIN;

ALTER TABLE public.participation
    DROP CONSTRAINT IF EXISTS participation_paiement_id_key,
    DROP CONSTRAINT IF EXISTS fk6rqo0obcdcsv8f30ykmq2up0n;

ALTER TABLE public.paiement
    DROP CONSTRAINT IF EXISTS paiement_methode_check,
    DROP CONSTRAINT IF EXISTS paiement_statut_check,
    DROP CONSTRAINT IF EXISTS paiement_montant_centimes_check,
    DROP CONSTRAINT IF EXISTS paiement_devise_check,
    DROP CONSTRAINT IF EXISTS paiement_provider_check,
    DROP CONSTRAINT IF EXISTS fk57valmvrpfcjhnemj2dmyf8fr,
    DROP CONSTRAINT IF EXISTS fk_paiement_reservation;

ALTER TABLE public.participation
    DROP COLUMN IF EXISTS paiement_id;

ALTER TABLE public.paiement
    ADD COLUMN IF NOT EXISTS reservation_id integer,
    ADD COLUMN IF NOT EXISTS montant_centimes integer,
    ADD COLUMN IF NOT EXISTS devise character varying(3) DEFAULT 'EUR'::character varying NOT NULL,
    ADD COLUMN IF NOT EXISTS provider character varying(30) DEFAULT 'MOCK'::character varying NOT NULL,
    ADD COLUMN IF NOT EXISTS provider_payment_id character varying(255),
    ADD COLUMN IF NOT EXISTS client_secret character varying(255),
    ADD COLUMN IF NOT EXISTS date_creation timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN IF NOT EXISTS date_expiration timestamp(6) without time zone,
    ADD COLUMN IF NOT EXISTS metadata jsonb;

UPDATE public.paiement p
SET reservation_id = m.reservation_id
FROM public.participation part
JOIN public.match_padel m ON m.id = part.match_id
WHERE p.participation_match_id = part.id
  AND p.reservation_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'paiement'
          AND column_name = 'montant'
    ) THEN
        EXECUTE 'UPDATE public.paiement
                 SET montant_centimes = ROUND(montant * 100)::integer
                 WHERE montant_centimes IS NULL
                   AND montant IS NOT NULL';
    END IF;
END $$;

ALTER TABLE public.paiement
    DROP COLUMN IF EXISTS montant,
    ALTER COLUMN reservation_id SET NOT NULL,
    ALTER COLUMN participation_match_id DROP NOT NULL,
    ALTER COLUMN montant_centimes SET NOT NULL,
    ALTER COLUMN date_paiement DROP NOT NULL,
    ALTER COLUMN methode SET DEFAULT 'CARTE',
    ALTER COLUMN statut SET DEFAULT 'EN_ATTENTE';

ALTER TABLE public.reservation
    ALTER COLUMN statut SET DEFAULT 'EN_ATTENTE_PAIEMENT';

ALTER TABLE public.paiement
    ADD CONSTRAINT paiement_montant_centimes_check CHECK (montant_centimes > 0),
    ADD CONSTRAINT paiement_devise_check CHECK (devise IN ('EUR')),
    ADD CONSTRAINT paiement_provider_check CHECK (provider IN ('MOCK', 'STRIPE')),
    ADD CONSTRAINT paiement_methode_check CHECK (methode IN ('CARTE', 'CASH', 'VIREMENT')),
    ADD CONSTRAINT paiement_statut_check CHECK (statut IN ('EN_ATTENTE', 'VALIDE', 'REFUSE', 'ANNULE', 'REMBOURSE')),
    ADD CONSTRAINT fk57valmvrpfcjhnemj2dmyf8fr FOREIGN KEY (participation_match_id) REFERENCES public.participation(id),
    ADD CONSTRAINT fk_paiement_reservation FOREIGN KEY (reservation_id) REFERENCES public.reservation(id);

ALTER TABLE public.reservation
    DROP CONSTRAINT IF EXISTS reservation_statut_check,
    ADD CONSTRAINT reservation_statut_check CHECK (statut IN ('EN_ATTENTE_PAIEMENT', 'VALIDEE', 'TERMINEE', 'ANNULEE'));

CREATE UNIQUE INDEX IF NOT EXISTS paiement_provider_payment_id_key
    ON public.paiement (provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;

INSERT INTO public.site (actif, id, adresse, code_postal, description, image_url, nom, ville)
VALUES (true, 4, 'Avenue des Tests 12', '5000', 'Site de test avec plusieurs terrains pour les scenarios Postman.', 'images/namur.jpg', 'NAMUR TEST PADEL', 'NAMUR')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.administrateur (id, matricule, site_id, type_admin, nom, prenom, email)
VALUES (5, 'AS05', 4, 'SITE', 'Admin', 'Namur', 'admin.namur@padel.be')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.horaire_site (annee, duree_match_minutes, heure_debut, heure_fin, id, pause_minutes, site_id)
VALUES
    (2026, 90, '07:30:00', '22:30:00', 4, 15, 4),
    (2027, 90, '08:00:00', '22:00:00', 5, 15, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.jour_fermeture (id, site_id, date_fermeture, raison, global)
VALUES
    (7, 4, '2026-09-27', 'Fermeture exceptionnelle Namur', false),
    (8, NULL, '2027-01-01', 'Nouvel an', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.terrain (actif, couvert, id, site_id, nom)
VALUES
    (true, true, 7, 4, 'N1'),
    (true, false, 8, 4, 'N2'),
    (false, true, 9, 4, 'N3-INACTIF'),
    (true, true, 10, 1, 'B3')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.membre (actif, id, site_id, type_membre_id, email, matricule, nom, prenom)
VALUES
    (true, 11, 4, 2, 'lea.fontaine@mail.com', 'S0005', 'Fontaine', 'Lea'),
    (true, 12, 4, 2, 'tom.vermeulen@mail.com', 'S0006', 'Vermeulen', 'Tom'),
    (true, 13, NULL, 3, 'ines.laurent@mail.com', 'L0004', 'Laurent', 'Ines'),
    (true, 14, NULL, 3, 'yanis.michel@mail.com', 'L0005', 'Michel', 'Yanis'),
    (true, 15, 1, 2, 'mila.dubois@mail.com', 'S0007', 'Dubois', 'Mila'),
    (true, 16, 2, 2, 'adam.rossi@mail.com', 'S0008', 'Rossi', 'Adam'),
    (false, 17, 4, 2, 'inactive.test@mail.com', 'S0099', 'Inactive', 'Test'),
    (true, 18, NULL, 1, 'global.test@mail.com', 'G0004', 'Global', 'Test')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.reservation (court_id, date, end_time, id, membre_id, start_time, statut)
VALUES
    (7, '2026-06-20', '10:30:00', 48, 11, '09:00:00', 'EN_ATTENTE_PAIEMENT'),
    (7, '2026-06-20', '12:30:00', 49, 11, '11:00:00', 'VALIDEE'),
    (8, '2026-06-20', '15:30:00', 50, 12, '14:00:00', 'ANNULEE'),
    (10, '2026-06-21', '11:30:00', 51, 3, '10:00:00', 'VALIDEE'),
    (2, '2026-05-20', '19:30:00', 52, 4, '18:00:00', 'TERMINEE'),
    (5, '2026-06-22', '19:30:00', 53, 13, '18:00:00', 'EN_ATTENTE_PAIEMENT'),
    (8, '2026-06-23', '09:00:00', 54, 12, '07:30:00', 'EN_ATTENTE_PAIEMENT'),
    (10, '2026-06-23', '17:30:00', 55, 14, '16:00:00', 'ANNULEE'),
    (1, '2026-06-24', '20:30:00', 56, 15, '19:00:00', 'ANNULEE'),
    (3, '2026-06-25', '11:00:00', 57, 16, '09:30:00', 'VALIDEE'),
    (6, '2026-06-26', '13:30:00', 58, 18, '12:00:00', 'VALIDEE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.match_padel (date_match, heure_debut, heure_fin, id, organisateur_id, prix_total, terrain_id, reservation_id, created_at, statut, type_match)
VALUES
    ('2026-06-20', '09:00:00', '10:30:00', 44, 11, 60, 7, 48, '2026-05-26 09:00:00', 'OUVERT', 'PUBLIC'),
    ('2026-06-20', '11:00:00', '12:30:00', 45, 11, 60, 7, 49, '2026-05-26 09:10:00', 'OUVERT', 'PUBLIC'),
    ('2026-06-20', '14:00:00', '15:30:00', 46, 12, 60, 8, 50, '2026-05-26 09:20:00', 'ANNULE', 'PUBLIC'),
    ('2026-06-21', '10:00:00', '11:30:00', 47, 3, 60, 10, 51, '2026-05-26 09:30:00', 'COMPLET', 'PRIVE'),
    ('2026-05-20', '18:00:00', '19:30:00', 48, 4, 60, 2, 52, '2026-05-19 09:30:00', 'TERMINE', 'PUBLIC'),
    ('2026-06-22', '18:00:00', '19:30:00', 49, 13, 60, 5, 53, '2026-05-26 10:00:00', 'OUVERT', 'PUBLIC'),
    ('2026-06-23', '16:00:00', '17:30:00', 50, 14, 60, 10, 55, '2026-05-26 10:30:00', 'ANNULE', 'PUBLIC'),
    ('2026-06-24', '19:00:00', '20:30:00', 51, 15, 60, 1, 56, '2026-05-26 11:00:00', 'ANNULE', 'PUBLIC'),
    ('2026-06-25', '09:30:00', '11:00:00', 52, 16, 60, 3, 57, '2026-05-26 11:30:00', 'OUVERT', 'PUBLIC')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.participation (id, match_id, membre_id, date_inscription)
VALUES
    (52, 44, 11, '2026-05-26 09:00:02'),
    (53, 45, 11, '2026-05-26 09:10:02'),
    (54, 45, 12, '2026-05-26 09:12:00'),
    (55, 45, 13, '2026-05-26 09:13:00'),
    (56, 46, 12, '2026-05-26 09:20:02'),
    (57, 47, 3, '2026-05-26 09:30:02'),
    (58, 47, 4, '2026-05-26 09:31:02'),
    (59, 47, 15, '2026-05-26 09:32:02'),
    (60, 47, 18, '2026-05-26 09:33:02'),
    (61, 49, 13, '2026-05-26 10:00:02'),
    (62, 50, 14, '2026-05-26 10:30:02'),
    (63, 51, 15, '2026-05-26 11:00:02'),
    (64, 52, 16, '2026-05-26 11:30:02'),
    (65, 52, 5, '2026-05-26 11:35:02')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.paiement
(id, reservation_id, participation_match_id, montant_centimes, devise, provider, provider_payment_id, client_secret, methode, statut, date_creation, date_paiement, date_expiration, metadata)
VALUES
    (1, 48, 52, 6000, 'EUR', 'MOCK', 'mock_pi_pending_48', 'mock_secret_pending_48', 'CARTE', 'EN_ATTENTE', '2026-05-26 09:00:05', NULL, '2026-05-26 09:15:05', '{"scenario": "reservation publique en attente"}'),
    (2, 49, 53, 6000, 'EUR', 'MOCK', 'mock_pi_paid_49', 'mock_secret_paid_49', 'CARTE', 'VALIDE', '2026-05-26 09:10:05', '2026-05-26 09:11:10', NULL, '{"scenario": "reservation publique payee"}'),
    (3, 53, 61, 6000, 'EUR', 'MOCK', 'mock_pi_refused_53', 'mock_secret_refused_53', 'CARTE', 'REFUSE', '2026-05-26 10:00:05', NULL, NULL, '{"scenario": "paiement refuse"}'),
    (4, 55, 62, 6000, 'EUR', 'MOCK', 'mock_pi_cancelled_55', 'mock_secret_cancelled_55', 'CARTE', 'ANNULE', '2026-05-26 10:30:05', NULL, '2026-05-26 10:45:05', '{"scenario": "paiement annule"}'),
    (5, 56, 63, 6000, 'EUR', 'MOCK', 'mock_pi_refunded_56', 'mock_secret_refunded_56', 'CARTE', 'REMBOURSE', '2026-05-26 11:00:05', '2026-05-26 11:01:10', NULL, '{"scenario": "paiement rembourse"}'),
    (6, 57, 64, 6000, 'EUR', 'MOCK', 'mock_pi_cash_57', NULL, 'CASH', 'VALIDE', '2026-05-26 11:30:05', '2026-05-26 11:32:00', NULL, '{"scenario": "paiement cash valide"}')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('public.administrateur', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.administrateur), true);
SELECT setval(pg_get_serial_sequence('public.horaire_site', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.horaire_site), true);
SELECT setval(pg_get_serial_sequence('public.jour_fermeture', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.jour_fermeture), true);
SELECT setval(pg_get_serial_sequence('public.match_padel', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.match_padel), true);
SELECT setval(pg_get_serial_sequence('public.membre', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.membre), true);
SELECT setval(pg_get_serial_sequence('public.paiement', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.paiement), true);
SELECT setval(pg_get_serial_sequence('public.participation', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.participation), true);
SELECT setval(pg_get_serial_sequence('public.reservation', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.reservation), true);
SELECT setval(pg_get_serial_sequence('public.site', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.site), true);
SELECT setval(pg_get_serial_sequence('public.terrain', 'id'), (SELECT COALESCE(MAX(id), 1) FROM public.terrain), true);

COMMIT;
