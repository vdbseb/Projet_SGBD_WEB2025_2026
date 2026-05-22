--
-- PostgreSQL database dump
--

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 17.0

-- Started on 2026-05-22 16:12:35

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
-- SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 16385)
-- Name: administrateur; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.administrateur (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    type_admin character varying(20) NOT NULL,
    site_id integer,
    CONSTRAINT chk_type_admin CHECK (((type_admin)::text = ANY (ARRAY[('GLOBAL'::character varying)::text, ('SITE'::character varying)::text])))
);


ALTER TABLE public.administrateur OWNER TO padel_app;

--
-- TOC entry 216 (class 1259 OID 16389)
-- Name: administrateur_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.administrateur_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.administrateur_id_seq OWNER TO padel_app;

--
-- TOC entry 3582 (class 0 OID 0)
-- Dependencies: 216
-- Name: administrateur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.administrateur_id_seq OWNED BY public.administrateur.id;


--
-- TOC entry 217 (class 1259 OID 16390)
-- Name: horaire_site; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.horaire_site (
    id integer NOT NULL,
    site_id integer NOT NULL,
    annee integer NOT NULL,
    heure_debut time without time zone NOT NULL,
    heure_fin time without time zone NOT NULL,
    duree_match_minutes integer DEFAULT 90 NOT NULL,
    pause_minutes integer DEFAULT 15 NOT NULL
);


ALTER TABLE public.horaire_site OWNER TO padel_app;

--
-- TOC entry 218 (class 1259 OID 16395)
-- Name: horaire_site_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.horaire_site_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.horaire_site_id_seq OWNER TO padel_app;

--
-- TOC entry 3583 (class 0 OID 0)
-- Dependencies: 218
-- Name: horaire_site_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.horaire_site_id_seq OWNED BY public.horaire_site.id;


--
-- TOC entry 219 (class 1259 OID 16396)
-- Name: jour_fermeture; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.jour_fermeture (
    id integer NOT NULL,
    site_id integer,
    date_fermeture date NOT NULL,
    raison character varying(255),
    global boolean DEFAULT false NOT NULL
);


ALTER TABLE public.jour_fermeture OWNER TO padel_app;

--
-- TOC entry 220 (class 1259 OID 16400)
-- Name: jour_fermeture_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.jour_fermeture_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.jour_fermeture_id_seq OWNER TO padel_app;

--
-- TOC entry 3584 (class 0 OID 0)
-- Dependencies: 220
-- Name: jour_fermeture_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.jour_fermeture_id_seq OWNED BY public.jour_fermeture.id;


--
-- TOC entry 221 (class 1259 OID 16401)
-- Name: match_padel; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.match_padel (
    id integer NOT NULL,
    terrain_id integer NOT NULL,
    organisateur_id integer NOT NULL,
    date_match date NOT NULL,
    heure_debut time without time zone NOT NULL,
    heure_fin time without time zone NOT NULL,
    type_match character varying(20) NOT NULL,
    statut character varying(30) NOT NULL,
    prix_total numeric(10,2) DEFAULT 60.00 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_statut_match CHECK (((statut)::text = ANY (ARRAY[('PLANIFIE'::character varying)::text, ('COMPLET'::character varying)::text, ('TERMINE'::character varying)::text, ('ANNULE'::character varying)::text]))),
    CONSTRAINT chk_type_match CHECK (((type_match)::text = ANY (ARRAY[('PUBLIC'::character varying)::text, ('PRIVE'::character varying)::text])))
);


ALTER TABLE public.match_padel OWNER TO padel_app;

--
-- TOC entry 222 (class 1259 OID 16408)
-- Name: match_padel_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.match_padel_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.match_padel_id_seq OWNER TO padel_app;

--
-- TOC entry 3585 (class 0 OID 0)
-- Dependencies: 222
-- Name: match_padel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.match_padel_id_seq OWNED BY public.match_padel.id;


--
-- TOC entry 237 (class 1259 OID 24750)
-- Name: member; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.member (
    matricule character varying(255) NOT NULL,
    actif boolean NOT NULL,
    email character varying(255) NOT NULL,
    nom character varying(255) NOT NULL,
    penalite_jours integer NOT NULL,
    prenom character varying(255) NOT NULL,
    solde_du double precision NOT NULL,
    type character varying(255) NOT NULL,
    site_id integer,
    CONSTRAINT member_type_check CHECK (((type)::text = ANY ((ARRAY['GLOBAL'::character varying, 'SITE'::character varying, 'LIBRE'::character varying])::text[])))
);


ALTER TABLE public.member OWNER TO padel_app;

--
-- TOC entry 223 (class 1259 OID 16409)
-- Name: membre; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.membre (
    id integer NOT NULL,
    matricule character varying(10) NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    email character varying(150),
    telephone character varying(30),
    type_membre_id integer NOT NULL,
    site_id integer,
    actif boolean DEFAULT true NOT NULL,
    date_creation timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.membre OWNER TO padel_app;

--
-- TOC entry 224 (class 1259 OID 16414)
-- Name: membre_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.membre_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membre_id_seq OWNER TO padel_app;

--
-- TOC entry 3586 (class 0 OID 0)
-- Dependencies: 224
-- Name: membre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.membre_id_seq OWNED BY public.membre.id;


--
-- TOC entry 225 (class 1259 OID 16415)
-- Name: paiement; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.paiement (
    id integer NOT NULL,
    participation_match_id integer NOT NULL,
    montant numeric(10,2) NOT NULL,
    date_paiement timestamp without time zone DEFAULT now() NOT NULL,
    methode character varying(30),
    statut character varying(20) NOT NULL,
    CONSTRAINT chk_statut_paiement_reel CHECK (((statut)::text = ANY (ARRAY[('VALIDE'::character varying)::text, ('REFUSE'::character varying)::text, ('REMBOURSE'::character varying)::text])))
);


ALTER TABLE public.paiement OWNER TO padel_app;

--
-- TOC entry 226 (class 1259 OID 16420)
-- Name: paiement_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.paiement_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.paiement_id_seq OWNER TO padel_app;

--
-- TOC entry 3587 (class 0 OID 0)
-- Dependencies: 226
-- Name: paiement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.paiement_id_seq OWNED BY public.paiement.id;


--
-- TOC entry 227 (class 1259 OID 16421)
-- Name: participation_match; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.participation_match (
    id integer NOT NULL,
    match_id integer NOT NULL,
    membre_id integer NOT NULL,
    date_inscription timestamp without time zone DEFAULT now() NOT NULL,
    montant_du numeric(10,2) DEFAULT 15.00 NOT NULL,
    montant_paye numeric(10,2) DEFAULT 0.00 NOT NULL,
    statut_paiement character varying(20) NOT NULL,
    organisateur boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_statut_paiement CHECK (((statut_paiement)::text = ANY (ARRAY[('EN_ATTENTE'::character varying)::text, ('PAYE'::character varying)::text, ('REMBOURSE'::character varying)::text])))
);


ALTER TABLE public.participation_match OWNER TO padel_app;

--
-- TOC entry 228 (class 1259 OID 16429)
-- Name: participation_match_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.participation_match_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.participation_match_id_seq OWNER TO padel_app;

--
-- TOC entry 3588 (class 0 OID 0)
-- Dependencies: 228
-- Name: participation_match_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.participation_match_id_seq OWNED BY public.participation_match.id;


--
-- TOC entry 229 (class 1259 OID 16430)
-- Name: penalite; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.penalite (
    id integer NOT NULL,
    membre_id integer NOT NULL,
    match_id integer,
    date_debut date NOT NULL,
    date_fin date NOT NULL,
    raison character varying(255) NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.penalite OWNER TO padel_app;

--
-- TOC entry 230 (class 1259 OID 16434)
-- Name: penalite_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.penalite_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.penalite_id_seq OWNER TO padel_app;

--
-- TOC entry 3589 (class 0 OID 0)
-- Dependencies: 230
-- Name: penalite_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.penalite_id_seq OWNED BY public.penalite.id;


--
-- TOC entry 238 (class 1259 OID 24758)
-- Name: reservation; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.reservation (
    id uuid NOT NULL,
    date date,
    end_time time(0) without time zone,
    start_time time(0) without time zone,
    court_id integer NOT NULL,
    member_id character varying(255) NOT NULL
);


ALTER TABLE public.reservation OWNER TO padel_app;

--
-- TOC entry 231 (class 1259 OID 16435)
-- Name: site; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.site (
    id integer NOT NULL,
    nom character varying(255) NOT NULL,
    adresse character varying(255) NOT NULL,
    ville character varying(255) NOT NULL,
    code_postal character varying(255) NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    description character varying(255),
    image_url character varying(255)
);


ALTER TABLE public.site OWNER TO padel_app;

--
-- TOC entry 232 (class 1259 OID 16441)
-- Name: site_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.site_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.site_id_seq OWNER TO padel_app;

--
-- TOC entry 3590 (class 0 OID 0)
-- Dependencies: 232
-- Name: site_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.site_id_seq OWNED BY public.site.id;


--
-- TOC entry 233 (class 1259 OID 16442)
-- Name: terrain; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.terrain (
    id integer NOT NULL,
    site_id integer NOT NULL,
    nom character varying(50) NOT NULL,
    couvert boolean DEFAULT false NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    name character varying(255)
);


ALTER TABLE public.terrain OWNER TO padel_app;

--
-- TOC entry 234 (class 1259 OID 16447)
-- Name: terrain_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.terrain_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.terrain_id_seq OWNER TO padel_app;

--
-- TOC entry 3591 (class 0 OID 0)
-- Dependencies: 234
-- Name: terrain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.terrain_id_seq OWNED BY public.terrain.id;


--
-- TOC entry 235 (class 1259 OID 16448)
-- Name: type_membre; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.type_membre (
    id integer NOT NULL,
    code character varying(20) NOT NULL,
    delai_reservation_jours integer NOT NULL
);


ALTER TABLE public.type_membre OWNER TO padel_app;

--
-- TOC entry 236 (class 1259 OID 16451)
-- Name: type_membre_id_seq; Type: SEQUENCE; Schema: public; Owner: padel_app
--

CREATE SEQUENCE public.type_membre_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.type_membre_id_seq OWNER TO padel_app;

--
-- TOC entry 3592 (class 0 OID 0)
-- Dependencies: 236
-- Name: type_membre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.type_membre_id_seq OWNED BY public.type_membre.id;


--
-- TOC entry 3325 (class 2604 OID 16452)
-- Name: administrateur id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur ALTER COLUMN id SET DEFAULT nextval('public.administrateur_id_seq'::regclass);


--
-- TOC entry 3326 (class 2604 OID 16453)
-- Name: horaire_site id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site ALTER COLUMN id SET DEFAULT nextval('public.horaire_site_id_seq'::regclass);


--
-- TOC entry 3329 (class 2604 OID 16454)
-- Name: jour_fermeture id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture ALTER COLUMN id SET DEFAULT nextval('public.jour_fermeture_id_seq'::regclass);


--
-- TOC entry 3331 (class 2604 OID 16455)
-- Name: match_padel id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel ALTER COLUMN id SET DEFAULT nextval('public.match_padel_id_seq'::regclass);


--
-- TOC entry 3334 (class 2604 OID 16456)
-- Name: membre id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre ALTER COLUMN id SET DEFAULT nextval('public.membre_id_seq'::regclass);


--
-- TOC entry 3337 (class 2604 OID 16457)
-- Name: paiement id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement ALTER COLUMN id SET DEFAULT nextval('public.paiement_id_seq'::regclass);


--
-- TOC entry 3339 (class 2604 OID 16458)
-- Name: participation_match id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match ALTER COLUMN id SET DEFAULT nextval('public.participation_match_id_seq'::regclass);


--
-- TOC entry 3344 (class 2604 OID 16459)
-- Name: penalite id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite ALTER COLUMN id SET DEFAULT nextval('public.penalite_id_seq'::regclass);


--
-- TOC entry 3346 (class 2604 OID 16460)
-- Name: site id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.site ALTER COLUMN id SET DEFAULT nextval('public.site_id_seq'::regclass);


--
-- TOC entry 3348 (class 2604 OID 16461)
-- Name: terrain id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain ALTER COLUMN id SET DEFAULT nextval('public.terrain_id_seq'::regclass);


--
-- TOC entry 3351 (class 2604 OID 16462)
-- Name: type_membre id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre ALTER COLUMN id SET DEFAULT nextval('public.type_membre_id_seq'::regclass);


--
-- TOC entry 3553 (class 0 OID 16385)
-- Dependencies: 215
-- Data for Name: administrateur; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.administrateur VALUES (1, 'Admin', 'Global', 'admin.global@padel.be', 'GLOBAL', NULL);
INSERT INTO public.administrateur VALUES (2, 'Admin', 'Bruxelles', 'admin.bruxelles@padel.be', 'SITE', 1);
INSERT INTO public.administrateur VALUES (3, 'Admin', 'Liege', 'admin.liege@padel.be', 'SITE', 2);
INSERT INTO public.administrateur VALUES (4, 'Admin', 'Arlon', 'admin.arlon@padel.be', 'SITE', 3);


--
-- TOC entry 3555 (class 0 OID 16390)
-- Dependencies: 217
-- Data for Name: horaire_site; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.horaire_site VALUES (1, 1, 2026, '08:00:00', '22:00:00', 90, 15);
INSERT INTO public.horaire_site VALUES (2, 2, 2026, '09:00:00', '21:00:00', 90, 15);
INSERT INTO public.horaire_site VALUES (3, 3, 2026, '08:30:00', '21:30:00', 90, 15);


--
-- TOC entry 3557 (class 0 OID 16396)
-- Dependencies: 219
-- Data for Name: jour_fermeture; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.jour_fermeture VALUES (1, NULL, '2026-01-01', 'Nouvel an', true);
INSERT INTO public.jour_fermeture VALUES (2, NULL, '2026-12-25', 'Noël', true);
INSERT INTO public.jour_fermeture VALUES (3, 1, '2026-07-21', 'Fermeture exceptionnelle Bruxelles', false);
INSERT INTO public.jour_fermeture VALUES (4, 2, '2026-08-15', 'Fermeture exceptionnelle Liège', false);
INSERT INTO public.jour_fermeture VALUES (5, 3, '2026-11-11', 'Fermeture exceptionnelle Arlon', false);


--
-- TOC entry 3559 (class 0 OID 16401)
-- Dependencies: 221
-- Data for Name: match_padel; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.match_padel VALUES (1, 1, 1, '2026-06-10', '10:00:00', '11:30:00', 'PUBLIC', 'PLANIFIE', 60.00, '2026-05-22 16:06:38.741332');
INSERT INTO public.match_padel VALUES (2, 2, 3, '2026-06-12', '14:00:00', '15:30:00', 'PRIVE', 'COMPLET', 60.00, '2026-05-22 16:06:38.741332');
INSERT INTO public.match_padel VALUES (3, 3, 5, '2026-06-15', '18:00:00', '19:30:00', 'PUBLIC', 'PLANIFIE', 60.00, '2026-05-22 16:06:38.741332');
INSERT INTO public.match_padel VALUES (4, 5, 2, '2026-06-18', '09:00:00', '10:30:00', 'PUBLIC', 'PLANIFIE', 60.00, '2026-05-22 16:06:38.741332');


--
-- TOC entry 3575 (class 0 OID 24750)
-- Dependencies: 237
-- Data for Name: member; Type: TABLE DATA; Schema: public; Owner: padel_app
--



--
-- TOC entry 3561 (class 0 OID 16409)
-- Dependencies: 223
-- Data for Name: membre; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.membre VALUES (1, 'G0001', 'Dupont', 'Jean', 'jean.dupont@mail.com', '0470000001', 1, NULL, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (2, 'G0002', 'Lambert', 'Sophie', 'sophie.lambert@mail.com', '0470000002', 1, NULL, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (3, 'S0001', 'Martin', 'Claire', 'claire.martin@mail.com', '0470000003', 2, 1, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (4, 'S0002', 'Bernard', 'Lucas', 'lucas.bernard@mail.com', '0470000004', 2, 1, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (5, 'S0003', 'Moreau', 'Emma', 'emma.moreau@mail.com', '0470000005', 2, 2, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (6, 'S0004', 'Lefevre', 'Noah', 'noah.lefevre@mail.com', '0470000006', 2, 3, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (7, 'L0001', 'Petit', 'Hugo', 'hugo.petit@mail.com', '0470000007', 3, NULL, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (8, 'L0002', 'Durand', 'Alice', 'alice.durand@mail.com', '0470000008', 3, NULL, true, '2026-05-22 16:06:38.741332');
INSERT INTO public.membre VALUES (9, 'L0003', 'Simon', 'Nathan', 'nathan.simon@mail.com', '0470000009', 3, NULL, true, '2026-05-22 16:06:38.741332');


--
-- TOC entry 3563 (class 0 OID 16415)
-- Dependencies: 225
-- Data for Name: paiement; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.paiement VALUES (1, 1, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (2, 2, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (3, 4, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (4, 5, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (5, 6, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (6, 7, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (7, 8, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');
INSERT INTO public.paiement VALUES (8, 10, 15.00, '2026-05-22 16:06:38.741332', 'CARTE', 'VALIDE');


--
-- TOC entry 3565 (class 0 OID 16421)
-- Dependencies: 227
-- Data for Name: participation_match; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.participation_match VALUES (1, 1, 1, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', true);
INSERT INTO public.participation_match VALUES (2, 1, 7, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', false);
INSERT INTO public.participation_match VALUES (3, 1, 8, '2026-05-22 16:06:38.741332', 15.00, 0.00, 'EN_ATTENTE', false);
INSERT INTO public.participation_match VALUES (4, 2, 3, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', true);
INSERT INTO public.participation_match VALUES (5, 2, 4, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', false);
INSERT INTO public.participation_match VALUES (6, 2, 1, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', false);
INSERT INTO public.participation_match VALUES (7, 2, 9, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', false);
INSERT INTO public.participation_match VALUES (8, 3, 5, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', true);
INSERT INTO public.participation_match VALUES (9, 3, 8, '2026-05-22 16:06:38.741332', 15.00, 0.00, 'EN_ATTENTE', false);
INSERT INTO public.participation_match VALUES (10, 4, 2, '2026-05-22 16:06:38.741332', 15.00, 15.00, 'PAYE', true);


--
-- TOC entry 3567 (class 0 OID 16430)
-- Dependencies: 229
-- Data for Name: penalite; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.penalite VALUES (1, 5, 3, '2026-06-16', '2026-06-23', 'Solde dû après match public incomplet', true);


--
-- TOC entry 3576 (class 0 OID 24758)
-- Dependencies: 238
-- Data for Name: reservation; Type: TABLE DATA; Schema: public; Owner: padel_app
--



--
-- TOC entry 3569 (class 0 OID 16435)
-- Dependencies: 231
-- Data for Name: site; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.site VALUES (1, 'THE ATOMIUM PADEL CLUB', '-', 'BRUXELLES', '-', true, 'Situé au cœur de la capitale, ce centre propose des terrains indoor de dernière génération. Idéal pour une partie entre collègues ou un tournoi intensif.', 'images/bruxelles.jpg');
INSERT INTO public.site VALUES (2, 'THE CARRÉ CLUB', '-', 'LIÈGE', '-', true, 'La "Cité Ardente" porte bien son nom ! Profitez de terrains spacieux et d\''un club-house réputé pour sa convivialité et son ambiance unique.', 'images/liege.jpg');
INSERT INTO public.site VALUES (3, 'ARLON BLUE PADEL', '-', 'ARLON', '-', true, 'À la frontière du Luxembourg, ce site offre un cadre verdoyant et apaisant. Des installations modernes parfaites pour s''évader du quotidien.', 'images/arlon.jpg');


--
-- TOC entry 3571 (class 0 OID 16442)
-- Dependencies: 233
-- Data for Name: terrain; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.terrain VALUES (1, 1, 'B1', true, true, NULL);
INSERT INTO public.terrain VALUES (2, 1, 'B2', false, true, NULL);
INSERT INTO public.terrain VALUES (3, 2, 'L1', true, true, NULL);
INSERT INTO public.terrain VALUES (4, 2, 'L2', false, true, NULL);
INSERT INTO public.terrain VALUES (5, 3, 'A1', true, true, NULL);
INSERT INTO public.terrain VALUES (6, 3, 'A2', false, true, NULL);


--
-- TOC entry 3573 (class 0 OID 16448)
-- Dependencies: 235
-- Data for Name: type_membre; Type: TABLE DATA; Schema: public; Owner: padel_app
--

INSERT INTO public.type_membre VALUES (1, 'GLOBAL', 21);
INSERT INTO public.type_membre VALUES (2, 'SITE', 14);
INSERT INTO public.type_membre VALUES (3, 'LIBRE', 5);


--
-- TOC entry 3593 (class 0 OID 0)
-- Dependencies: 216
-- Name: administrateur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.administrateur_id_seq', 4, true);


--
-- TOC entry 3594 (class 0 OID 0)
-- Dependencies: 218
-- Name: horaire_site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.horaire_site_id_seq', 3, true);


--
-- TOC entry 3595 (class 0 OID 0)
-- Dependencies: 220
-- Name: jour_fermeture_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.jour_fermeture_id_seq', 5, true);


--
-- TOC entry 3596 (class 0 OID 0)
-- Dependencies: 222
-- Name: match_padel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.match_padel_id_seq', 4, true);


--
-- TOC entry 3597 (class 0 OID 0)
-- Dependencies: 224
-- Name: membre_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.membre_id_seq', 9, true);


--
-- TOC entry 3598 (class 0 OID 0)
-- Dependencies: 226
-- Name: paiement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.paiement_id_seq', 8, true);


--
-- TOC entry 3599 (class 0 OID 0)
-- Dependencies: 228
-- Name: participation_match_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.participation_match_id_seq', 10, true);


--
-- TOC entry 3600 (class 0 OID 0)
-- Dependencies: 230
-- Name: penalite_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.penalite_id_seq', 1, true);


--
-- TOC entry 3601 (class 0 OID 0)
-- Dependencies: 232
-- Name: site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.site_id_seq', 3, true);


--
-- TOC entry 3602 (class 0 OID 0)
-- Dependencies: 234
-- Name: terrain_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.terrain_id_seq', 6, true);


--
-- TOC entry 3603 (class 0 OID 0)
-- Dependencies: 236
-- Name: type_membre_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.type_membre_id_seq', 3, true);


--
-- TOC entry 3359 (class 2606 OID 16464)
-- Name: administrateur administrateur_email_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_email_key UNIQUE (email);


--
-- TOC entry 3361 (class 2606 OID 16466)
-- Name: administrateur administrateur_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_pkey PRIMARY KEY (id);


--
-- TOC entry 3363 (class 2606 OID 16468)
-- Name: horaire_site horaire_site_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site
    ADD CONSTRAINT horaire_site_pkey PRIMARY KEY (id);


--
-- TOC entry 3365 (class 2606 OID 16470)
-- Name: jour_fermeture jour_fermeture_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture
    ADD CONSTRAINT jour_fermeture_pkey PRIMARY KEY (id);


--
-- TOC entry 3367 (class 2606 OID 16472)
-- Name: match_padel match_padel_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_pkey PRIMARY KEY (id);


--
-- TOC entry 3389 (class 2606 OID 24757)
-- Name: member member_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.member
    ADD CONSTRAINT member_pkey PRIMARY KEY (matricule);


--
-- TOC entry 3369 (class 2606 OID 16474)
-- Name: membre membre_matricule_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_matricule_key UNIQUE (matricule);


--
-- TOC entry 3371 (class 2606 OID 16476)
-- Name: membre membre_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_pkey PRIMARY KEY (id);


--
-- TOC entry 3373 (class 2606 OID 16478)
-- Name: paiement paiement_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_pkey PRIMARY KEY (id);


--
-- TOC entry 3375 (class 2606 OID 16480)
-- Name: participation_match participation_match_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_pkey PRIMARY KEY (id);


--
-- TOC entry 3379 (class 2606 OID 16482)
-- Name: penalite penalite_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_pkey PRIMARY KEY (id);


--
-- TOC entry 3393 (class 2606 OID 24762)
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- TOC entry 3381 (class 2606 OID 16484)
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- TOC entry 3383 (class 2606 OID 16486)
-- Name: terrain terrain_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain
    ADD CONSTRAINT terrain_pkey PRIMARY KEY (id);


--
-- TOC entry 3385 (class 2606 OID 16488)
-- Name: type_membre type_membre_code_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre
    ADD CONSTRAINT type_membre_code_key UNIQUE (code);


--
-- TOC entry 3387 (class 2606 OID 16490)
-- Name: type_membre type_membre_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre
    ADD CONSTRAINT type_membre_pkey PRIMARY KEY (id);


--
-- TOC entry 3391 (class 2606 OID 24776)
-- Name: member ukmbmcqelty0fbrvxp1q58dn57t; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.member
    ADD CONSTRAINT ukmbmcqelty0fbrvxp1q58dn57t UNIQUE (email);


--
-- TOC entry 3377 (class 2606 OID 16492)
-- Name: participation_match unique_membre_match; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT unique_membre_match UNIQUE (match_id, membre_id);


--
-- TOC entry 3394 (class 2606 OID 16493)
-- Name: administrateur administrateur_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3408 (class 2606 OID 24787)
-- Name: reservation fk68999qe28ym9eqqlowybh9nvn; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT fk68999qe28ym9eqqlowybh9nvn FOREIGN KEY (member_id) REFERENCES public.member(matricule);


--
-- TOC entry 3409 (class 2606 OID 24782)
-- Name: reservation fkmi7rw0to79afldrhpopfgvn0o; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT fkmi7rw0to79afldrhpopfgvn0o FOREIGN KEY (court_id) REFERENCES public.terrain(id);


--
-- TOC entry 3407 (class 2606 OID 24777)
-- Name: member fksdl3x4tka5l2w3dmxgiy92h3c; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.member
    ADD CONSTRAINT fksdl3x4tka5l2w3dmxgiy92h3c FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3395 (class 2606 OID 16498)
-- Name: horaire_site horaire_site_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site
    ADD CONSTRAINT horaire_site_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3396 (class 2606 OID 16503)
-- Name: jour_fermeture jour_fermeture_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture
    ADD CONSTRAINT jour_fermeture_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3397 (class 2606 OID 16508)
-- Name: match_padel match_padel_organisateur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_organisateur_id_fkey FOREIGN KEY (organisateur_id) REFERENCES public.membre(id);


--
-- TOC entry 3398 (class 2606 OID 16513)
-- Name: match_padel match_padel_terrain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_terrain_id_fkey FOREIGN KEY (terrain_id) REFERENCES public.terrain(id);


--
-- TOC entry 3399 (class 2606 OID 16518)
-- Name: membre membre_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3400 (class 2606 OID 16523)
-- Name: membre membre_type_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_type_membre_id_fkey FOREIGN KEY (type_membre_id) REFERENCES public.type_membre(id);


--
-- TOC entry 3401 (class 2606 OID 16528)
-- Name: paiement paiement_participation_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_participation_match_id_fkey FOREIGN KEY (participation_match_id) REFERENCES public.participation_match(id) ON DELETE CASCADE;


--
-- TOC entry 3402 (class 2606 OID 16533)
-- Name: participation_match participation_match_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.match_padel(id) ON DELETE CASCADE;


--
-- TOC entry 3403 (class 2606 OID 16538)
-- Name: participation_match participation_match_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membre(id);


--
-- TOC entry 3404 (class 2606 OID 16543)
-- Name: penalite penalite_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.match_padel(id);


--
-- TOC entry 3405 (class 2606 OID 16548)
-- Name: penalite penalite_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membre(id);


--
-- TOC entry 3406 (class 2606 OID 16553)
-- Name: terrain terrain_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain
    ADD CONSTRAINT terrain_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);



