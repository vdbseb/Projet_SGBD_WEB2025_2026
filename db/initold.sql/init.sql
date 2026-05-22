--
-- PostgreSQL database dump
--

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 17.0

-- Started on 2026-05-20 17:58:33

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- TOC entry 228 (class 1259 OID 16685)
-- Name: administrateur; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.administrateur (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    type_admin character varying(20) NOT NULL,
    site_id integer,
    CONSTRAINT chk_type_admin CHECK (((type_admin)::text = ANY ((ARRAY['GLOBAL'::character varying, 'SITE'::character varying])::text[])))
);


ALTER TABLE public.administrateur OWNER TO padel_app;

--
-- TOC entry 227 (class 1259 OID 16684)
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
-- TOC entry 3562 (class 0 OID 0)
-- Dependencies: 227
-- Name: administrateur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.administrateur_id_seq OWNED BY public.administrateur.id;


--
-- TOC entry 224 (class 1259 OID 16658)
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
-- TOC entry 223 (class 1259 OID 16657)
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
-- TOC entry 3563 (class 0 OID 0)
-- Dependencies: 223
-- Name: horaire_site_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.horaire_site_id_seq OWNED BY public.horaire_site.id;


--
-- TOC entry 226 (class 1259 OID 16672)
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
-- TOC entry 225 (class 1259 OID 16671)
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
-- TOC entry 3564 (class 0 OID 0)
-- Dependencies: 225
-- Name: jour_fermeture_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.jour_fermeture_id_seq OWNED BY public.jour_fermeture.id;


--
-- TOC entry 230 (class 1259 OID 16700)
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
    CONSTRAINT chk_statut_match CHECK (((statut)::text = ANY ((ARRAY['PLANIFIE'::character varying, 'COMPLET'::character varying, 'TERMINE'::character varying, 'ANNULE'::character varying])::text[]))),
    CONSTRAINT chk_type_match CHECK (((type_match)::text = ANY ((ARRAY['PUBLIC'::character varying, 'PRIVE'::character varying])::text[])))
);


ALTER TABLE public.match_padel OWNER TO padel_app;

--
-- TOC entry 229 (class 1259 OID 16699)
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
-- TOC entry 3565 (class 0 OID 0)
-- Dependencies: 229
-- Name: match_padel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.match_padel_id_seq OWNED BY public.match_padel.id;


--
-- TOC entry 222 (class 1259 OID 16637)
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
-- TOC entry 221 (class 1259 OID 16636)
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
-- TOC entry 3566 (class 0 OID 0)
-- Dependencies: 221
-- Name: membre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.membre_id_seq OWNED BY public.membre.id;


--
-- TOC entry 234 (class 1259 OID 16745)
-- Name: paiement; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.paiement (
    id integer NOT NULL,
    participation_match_id integer NOT NULL,
    montant numeric(10,2) NOT NULL,
    date_paiement timestamp without time zone DEFAULT now() NOT NULL,
    methode character varying(30),
    statut character varying(20) NOT NULL,
    CONSTRAINT chk_statut_paiement_reel CHECK (((statut)::text = ANY ((ARRAY['VALIDE'::character varying, 'REFUSE'::character varying, 'REMBOURSE'::character varying])::text[])))
);


ALTER TABLE public.paiement OWNER TO padel_app;

--
-- TOC entry 233 (class 1259 OID 16744)
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
-- TOC entry 3567 (class 0 OID 0)
-- Dependencies: 233
-- Name: paiement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.paiement_id_seq OWNED BY public.paiement.id;


--
-- TOC entry 232 (class 1259 OID 16721)
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
    CONSTRAINT chk_statut_paiement CHECK (((statut_paiement)::text = ANY ((ARRAY['EN_ATTENTE'::character varying, 'PAYE'::character varying, 'REMBOURSE'::character varying])::text[])))
);


ALTER TABLE public.participation_match OWNER TO padel_app;

--
-- TOC entry 231 (class 1259 OID 16720)
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
-- TOC entry 3568 (class 0 OID 0)
-- Dependencies: 231
-- Name: participation_match_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.participation_match_id_seq OWNED BY public.participation_match.id;


--
-- TOC entry 236 (class 1259 OID 16759)
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
-- TOC entry 235 (class 1259 OID 16758)
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
-- TOC entry 3569 (class 0 OID 0)
-- Dependencies: 235
-- Name: penalite_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.penalite_id_seq OWNED BY public.penalite.id;


--
-- TOC entry 216 (class 1259 OID 16606)
-- Name: site; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.site (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    adresse character varying(255) NOT NULL,
    ville character varying(100) NOT NULL,
    code_postal character varying(10) NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    description text,
    image_url character varying(500)
);


ALTER TABLE public.site OWNER TO padel_app;

--
-- TOC entry 215 (class 1259 OID 16605)
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
-- TOC entry 3570 (class 0 OID 0)
-- Dependencies: 215
-- Name: site_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.site_id_seq OWNED BY public.site.id;


--
-- TOC entry 218 (class 1259 OID 16614)
-- Name: terrain; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.terrain (
    id integer NOT NULL,
    site_id integer NOT NULL,
    nom character varying(50) NOT NULL,
    couvert boolean DEFAULT false NOT NULL,
    actif boolean DEFAULT true NOT NULL
);


ALTER TABLE public.terrain OWNER TO padel_app;

--
-- TOC entry 217 (class 1259 OID 16613)
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
-- TOC entry 3571 (class 0 OID 0)
-- Dependencies: 217
-- Name: terrain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.terrain_id_seq OWNED BY public.terrain.id;


--
-- TOC entry 220 (class 1259 OID 16628)
-- Name: type_membre; Type: TABLE; Schema: public; Owner: padel_app
--

CREATE TABLE public.type_membre (
    id integer NOT NULL,
    code character varying(20) NOT NULL,
    delai_reservation_jours integer NOT NULL
);


ALTER TABLE public.type_membre OWNER TO padel_app;

--
-- TOC entry 219 (class 1259 OID 16627)
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
-- TOC entry 3572 (class 0 OID 0)
-- Dependencies: 219
-- Name: type_membre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: padel_app
--

ALTER SEQUENCE public.type_membre_id_seq OWNED BY public.type_membre.id;


--
-- TOC entry 3331 (class 2604 OID 16688)
-- Name: administrateur id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur ALTER COLUMN id SET DEFAULT nextval('public.administrateur_id_seq'::regclass);


--
-- TOC entry 3326 (class 2604 OID 16661)
-- Name: horaire_site id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site ALTER COLUMN id SET DEFAULT nextval('public.horaire_site_id_seq'::regclass);


--
-- TOC entry 3329 (class 2604 OID 16675)
-- Name: jour_fermeture id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture ALTER COLUMN id SET DEFAULT nextval('public.jour_fermeture_id_seq'::regclass);


--
-- TOC entry 3332 (class 2604 OID 16703)
-- Name: match_padel id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel ALTER COLUMN id SET DEFAULT nextval('public.match_padel_id_seq'::regclass);


--
-- TOC entry 3323 (class 2604 OID 16640)
-- Name: membre id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre ALTER COLUMN id SET DEFAULT nextval('public.membre_id_seq'::regclass);


--
-- TOC entry 3340 (class 2604 OID 16748)
-- Name: paiement id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement ALTER COLUMN id SET DEFAULT nextval('public.paiement_id_seq'::regclass);


--
-- TOC entry 3335 (class 2604 OID 16724)
-- Name: participation_match id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match ALTER COLUMN id SET DEFAULT nextval('public.participation_match_id_seq'::regclass);


--
-- TOC entry 3342 (class 2604 OID 16762)
-- Name: penalite id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite ALTER COLUMN id SET DEFAULT nextval('public.penalite_id_seq'::regclass);


--
-- TOC entry 3317 (class 2604 OID 16609)
-- Name: site id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.site ALTER COLUMN id SET DEFAULT nextval('public.site_id_seq'::regclass);


--
-- TOC entry 3319 (class 2604 OID 16617)
-- Name: terrain id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain ALTER COLUMN id SET DEFAULT nextval('public.terrain_id_seq'::regclass);


--
-- TOC entry 3322 (class 2604 OID 16631)
-- Name: type_membre id; Type: DEFAULT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre ALTER COLUMN id SET DEFAULT nextval('public.type_membre_id_seq'::regclass);


--
-- TOC entry 3548 (class 0 OID 16685)
-- Dependencies: 228
-- Data for Name: administrateur; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.administrateur (id, nom, prenom, email, type_admin, site_id) FROM stdin;
\.


--
-- TOC entry 3544 (class 0 OID 16658)
-- Dependencies: 224
-- Data for Name: horaire_site; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.horaire_site (id, site_id, annee, heure_debut, heure_fin, duree_match_minutes, pause_minutes) FROM stdin;
\.


--
-- TOC entry 3546 (class 0 OID 16672)
-- Dependencies: 226
-- Data for Name: jour_fermeture; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.jour_fermeture (id, site_id, date_fermeture, raison, global) FROM stdin;
\.


--
-- TOC entry 3550 (class 0 OID 16700)
-- Dependencies: 230
-- Data for Name: match_padel; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.match_padel (id, terrain_id, organisateur_id, date_match, heure_debut, heure_fin, type_match, statut, prix_total, created_at) FROM stdin;
\.


--
-- TOC entry 3542 (class 0 OID 16637)
-- Dependencies: 222
-- Data for Name: membre; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.membre (id, matricule, nom, prenom, email, telephone, type_membre_id, site_id, actif, date_creation) FROM stdin;
\.


--
-- TOC entry 3554 (class 0 OID 16745)
-- Dependencies: 234
-- Data for Name: paiement; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.paiement (id, participation_match_id, montant, date_paiement, methode, statut) FROM stdin;
\.


--
-- TOC entry 3552 (class 0 OID 16721)
-- Dependencies: 232
-- Data for Name: participation_match; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.participation_match (id, match_id, membre_id, date_inscription, montant_du, montant_paye, statut_paiement, organisateur) FROM stdin;
\.


--
-- TOC entry 3556 (class 0 OID 16759)
-- Dependencies: 236
-- Data for Name: penalite; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.penalite (id, membre_id, match_id, date_debut, date_fin, raison, active) FROM stdin;
\.


--
-- TOC entry 3536 (class 0 OID 16606)
-- Dependencies: 216
-- Data for Name: site; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.site (id, nom, adresse, ville, code_postal, actif, description, image_url) FROM stdin;
1	THE ATOMIUM PADEL CLUB	-	BRUXELLES	-	t	Situé au cœur de la capitale, ce centre propose des terrains indoor de dernière génération. Idéal pour une partie entre collègues ou un tournoi intensif.	images/bruxelles.jpg
2	THE CARRÉ CLUB	-	LIÈGE	-	t	La "Cité Ardente" porte bien son nom ! Profitez de terrains spacieux et d\\'un club-house réputé pour sa convivialité et son ambiance unique.	images/liege.jpg
3	ARLON BLUE PADEL	-	ARLON	-	t	À la frontière du Luxembourg, ce site offre un cadre verdoyant et apaisant. Des installations modernes parfaites pour s'évader du quotidien.	images/arlon.jpg
\.


--
-- TOC entry 3538 (class 0 OID 16614)
-- Dependencies: 218
-- Data for Name: terrain; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.terrain (id, site_id, nom, couvert, actif) FROM stdin;
1	1	B1	t	t
2	1	B2	f	t
3	2	L1	t	t
4	2	L2	f	t
5	3	A1	t	t
6	3	A2	f	t
\.


--
-- TOC entry 3540 (class 0 OID 16628)
-- Dependencies: 220
-- Data for Name: type_membre; Type: TABLE DATA; Schema: public; Owner: padel_app
--

COPY public.type_membre (id, code, delai_reservation_jours) FROM stdin;
1	GLOBAL	21
2	SITE	14
3	LIBRE	5
\.


--
-- TOC entry 3573 (class 0 OID 0)
-- Dependencies: 227
-- Name: administrateur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.administrateur_id_seq', 1, false);


--
-- TOC entry 3574 (class 0 OID 0)
-- Dependencies: 223
-- Name: horaire_site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.horaire_site_id_seq', 1, false);


--
-- TOC entry 3575 (class 0 OID 0)
-- Dependencies: 225
-- Name: jour_fermeture_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.jour_fermeture_id_seq', 1, false);


--
-- TOC entry 3576 (class 0 OID 0)
-- Dependencies: 229
-- Name: match_padel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.match_padel_id_seq', 1, false);


--
-- TOC entry 3577 (class 0 OID 0)
-- Dependencies: 221
-- Name: membre_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.membre_id_seq', 1, false);


--
-- TOC entry 3578 (class 0 OID 0)
-- Dependencies: 233
-- Name: paiement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.paiement_id_seq', 1, false);


--
-- TOC entry 3579 (class 0 OID 0)
-- Dependencies: 231
-- Name: participation_match_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.participation_match_id_seq', 1, false);


--
-- TOC entry 3580 (class 0 OID 0)
-- Dependencies: 235
-- Name: penalite_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.penalite_id_seq', 1, false);


--
-- TOC entry 3581 (class 0 OID 0)
-- Dependencies: 215
-- Name: site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.site_id_seq', 4, true);


--
-- TOC entry 3582 (class 0 OID 0)
-- Dependencies: 217
-- Name: terrain_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.terrain_id_seq', 6, true);


--
-- TOC entry 3583 (class 0 OID 0)
-- Dependencies: 219
-- Name: type_membre_id_seq; Type: SEQUENCE SET; Schema: public; Owner: padel_app
--

SELECT pg_catalog.setval('public.type_membre_id_seq', 3, true);


--
-- TOC entry 3366 (class 2606 OID 16693)
-- Name: administrateur administrateur_email_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_email_key UNIQUE (email);


--
-- TOC entry 3368 (class 2606 OID 16691)
-- Name: administrateur administrateur_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_pkey PRIMARY KEY (id);


--
-- TOC entry 3362 (class 2606 OID 16665)
-- Name: horaire_site horaire_site_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site
    ADD CONSTRAINT horaire_site_pkey PRIMARY KEY (id);


--
-- TOC entry 3364 (class 2606 OID 16678)
-- Name: jour_fermeture jour_fermeture_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture
    ADD CONSTRAINT jour_fermeture_pkey PRIMARY KEY (id);


--
-- TOC entry 3370 (class 2606 OID 16709)
-- Name: match_padel match_padel_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_pkey PRIMARY KEY (id);


--
-- TOC entry 3358 (class 2606 OID 16646)
-- Name: membre membre_matricule_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_matricule_key UNIQUE (matricule);


--
-- TOC entry 3360 (class 2606 OID 16644)
-- Name: membre membre_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_pkey PRIMARY KEY (id);


--
-- TOC entry 3376 (class 2606 OID 16752)
-- Name: paiement paiement_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_pkey PRIMARY KEY (id);


--
-- TOC entry 3372 (class 2606 OID 16731)
-- Name: participation_match participation_match_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_pkey PRIMARY KEY (id);


--
-- TOC entry 3378 (class 2606 OID 16765)
-- Name: penalite penalite_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_pkey PRIMARY KEY (id);


--
-- TOC entry 3350 (class 2606 OID 16612)
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- TOC entry 3352 (class 2606 OID 16621)
-- Name: terrain terrain_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain
    ADD CONSTRAINT terrain_pkey PRIMARY KEY (id);


--
-- TOC entry 3354 (class 2606 OID 16635)
-- Name: type_membre type_membre_code_key; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre
    ADD CONSTRAINT type_membre_code_key UNIQUE (code);


--
-- TOC entry 3356 (class 2606 OID 16633)
-- Name: type_membre type_membre_pkey; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.type_membre
    ADD CONSTRAINT type_membre_pkey PRIMARY KEY (id);


--
-- TOC entry 3374 (class 2606 OID 16733)
-- Name: participation_match unique_membre_match; Type: CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT unique_membre_match UNIQUE (match_id, membre_id);


--
-- TOC entry 3384 (class 2606 OID 16694)
-- Name: administrateur administrateur_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.administrateur
    ADD CONSTRAINT administrateur_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3382 (class 2606 OID 16666)
-- Name: horaire_site horaire_site_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.horaire_site
    ADD CONSTRAINT horaire_site_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3383 (class 2606 OID 16679)
-- Name: jour_fermeture jour_fermeture_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.jour_fermeture
    ADD CONSTRAINT jour_fermeture_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3385 (class 2606 OID 16715)
-- Name: match_padel match_padel_organisateur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_organisateur_id_fkey FOREIGN KEY (organisateur_id) REFERENCES public.membre(id);


--
-- TOC entry 3386 (class 2606 OID 16710)
-- Name: match_padel match_padel_terrain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.match_padel
    ADD CONSTRAINT match_padel_terrain_id_fkey FOREIGN KEY (terrain_id) REFERENCES public.terrain(id);


--
-- TOC entry 3380 (class 2606 OID 16652)
-- Name: membre membre_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- TOC entry 3381 (class 2606 OID 16647)
-- Name: membre membre_type_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.membre
    ADD CONSTRAINT membre_type_membre_id_fkey FOREIGN KEY (type_membre_id) REFERENCES public.type_membre(id);


--
-- TOC entry 3389 (class 2606 OID 16753)
-- Name: paiement paiement_participation_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_participation_match_id_fkey FOREIGN KEY (participation_match_id) REFERENCES public.participation_match(id) ON DELETE CASCADE;


--
-- TOC entry 3387 (class 2606 OID 16734)
-- Name: participation_match participation_match_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.match_padel(id) ON DELETE CASCADE;


--
-- TOC entry 3388 (class 2606 OID 16739)
-- Name: participation_match participation_match_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.participation_match
    ADD CONSTRAINT participation_match_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membre(id);


--
-- TOC entry 3390 (class 2606 OID 16771)
-- Name: penalite penalite_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.match_padel(id);


--
-- TOC entry 3391 (class 2606 OID 16766)
-- Name: penalite penalite_membre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.penalite
    ADD CONSTRAINT penalite_membre_id_fkey FOREIGN KEY (membre_id) REFERENCES public.membre(id);


--
-- TOC entry 3379 (class 2606 OID 16622)
-- Name: terrain terrain_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: padel_app
--

ALTER TABLE ONLY public.terrain
    ADD CONSTRAINT terrain_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


-- Completed on 2026-05-20 17:58:33

--
-- PostgreSQL database dump complete
--

