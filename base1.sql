CREATE DATABASE biblio;
\c biblio;
ALTER SEQUENCE statut_pret_id_statut_pret_seq RESTART WITH 1;

CREATE TABLE Admin (
    id_admin SERIAL PRIMARY KEY,
    nom_utilisateur VARCHAR(100) NOT NULL,
    mot_de_passe TEXT NOT NULL
);

CREATE TABLE Type_Adherent (
    id SERIAL PRIMARY KEY,
    nom_type VARCHAR(50) NOT NULL,
    quota_max_pret INT NOT NULL,
    duree_max_pret INT NOT NULL,
    quota_max_prolongement INT NOT NULL, 
    duree_max_prolongement INT NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Adherent (
    id SERIAL PRIMARY KEY,  
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    password VARCHAR(200), 
    date_naissance DATE NOT NULL,
    date_inscription DATE NOT NULL,
    id_type INT REFERENCES Type_Adherent(id),
    est_abonne BOOLEAN DEFAULT FALSE,
    est_penalise BOOLEAN DEFAULT FALSE
);
CREATE TABLE Abonne(
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    date_debut DATE,
    date_fin DATE,
    valide BOOLEAN
);
INSERT INTO Abonne (id_adherent, date_debut, date_fin, valide) VALUES 
(1, '2025-02-01', '2025-07-24', TRUE),
(2, '2025-02-01', '2025-07-01', FALSE),
(3, '2025-04-01', '2025-12-01', TRUE),
(4, '2025-07-01', '2026-07-01', TRUE),
(5, '2025-08-01', '2026-05-01', FALSE),
(6, '2025-07-01', '2026-06-01', TRUE),
(7, '2025-06-01', '2025-12-01', TRUE),
(8, '2024-10-01', '2025-06-01', FALSE);
insert into Abonne(id_adherent,date_debut,date_fin) values (23,'2025-01-04','2025-11-04');

CREATE TABLE jourNonOuvrable (
    id SERIAL PRIMARY KEY,
    date_jour_non_ouvrable DATE NOT NULL,
    typeJour VARCHAR(255)
);
-- Inserting the holiday (Dimanche as jour férié)
INSERT INTO jourNonOuvrable (date_jour_non_ouvrable, typeJour) VALUES 
('2025-07-26', 'Jour ferie'),
('2025-07-19', 'Jour ferie');

-- Inserting the specific non-working dates
INSERT INTO jourNonOuvrable (date_jour_non_ouvrable, typeJour) VALUES 
('2025-07-13', 'Dimanche'),
('2025-07-20', ' Dimanche'),
('2025-07-27', 'Dimanche'),
('2025-08-03', 'Dimanche'),
('2025-08-10', 'Dimanche'),
('2025-08-17', 'Dimanche');

CREATE TABLE Livre (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(100),
    isbn VARCHAR(13),
    date_publication DATE,
    age_restriction INT,
    nbr_exmp int DEFAULT 1 , 
    description TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 

alter table Livre add column  image_url VARCHAR(255);


CREATE TABLE Type_Utilisation (
    id SERIAL PRIMARY KEY,
    nom_type_utilisation VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Droit_Pret (
    id SERIAL PRIMARY KEY,
    id_type_adherent INT REFERENCES Type_Adherent(id),
    id_type_utilisation INT REFERENCES Type_Utilisation(id),
    id_livre INT REFERENCES Livre(id),
    autorise BOOLEAN DEFAULT FALSE
);

CREATE TABLE Statut_Pret (
    id_statut_pret SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE
);
insert into Statut_Pret (nom_statut) values ('disponible'), ('emprunté');

CREATE TABLE Exemplaire (
    id_exemplaire SERIAL PRIMARY KEY,
    id_livre INT REFERENCES Livre(id),
    code_exemplaire VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_status INT REFERENCES Statut_Pret(id_statut_pret) DEFAULT 1 -
);




CREATE TABLE Type_Pret (
    id_type_pret SERIAL PRIMARY KEY,
    nom_type_pret VARCHAR(50),
    duree_max INT
);

insert into Type_Pret (nom_type_pret, duree_max) VALUES
('lecture sur place', 14),('prêt à domicile', 21);
    CREATE TABLE Pret (
        id SERIAL PRIMARY KEY,
        id_adherent INT REFERENCES Adherent(id),
        id_exemplaire INT REFERENCES Exemplaire(id_exemplaire),
        date_pret DATE NOT NULL,
        date_retour_prevue DATE NOT NULL,
        date_retour_reelle DATE,
        id_type_pret INT REFERENCES Type_Pret(id_type_pret),
        est_prolonge BOOLEAN DEFAULT FALSE, 
        statut INT REFERENCES Statut_Pret(id_statut_pret)
    );


   ALTER TABLE IF EXISTS Pret
    ALTER COLUMN est_prolonge SET DATA TYPE BOOLEAN
    USING est_prolonge::BOOLEAN;


-- Table des prolongements de prêts
CREATE TABLE Prolongement_Pret (
    id SERIAL PRIMARY KEY,
    id_pret INT REFERENCES Pret(id),
    date_prolongement DATE NOT NULL,
    date_retour_prevue DATE NOT NULL,
    statut INT REFERENCES Statut_Pret(id_statut_pret)
);

CREATE TABLE Historique_Statut_Pret (
    id_historique SERIAL PRIMARY KEY,
    id_pret INT NOT NULL REFERENCES Pret(id) ON DELETE CASCADE,
    id_statut_pret INT NOT NULL REFERENCES Statut_Pret(id_statut_pret),
    id_admin INT NOT NULL REFERENCES Admin(id_admin),
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Type_Penalite (
    id_type_penalite SERIAL PRIMARY KEY,
    nom_type_penalite VARCHAR(100),
    durre_penalite INT, 
    description TEXT
);

CREATE TABLE Penalite (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    raison VARCHAR(250), 
    id_type_penalite INT REFERENCES Type_Penalite(id_type_penalite),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL
);

CREATE TABLE Type_Payement (
    id SERIAL PRIMARY KEY,
    type_name VARCHAR(250), 
    montant_payement INT,
);

CREATE TABLE Statut_Paiement (
    id SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE 
);
insert into  Statut_Paiement (nom_statut) values ('en_attente'), ('valide'), ('rejeté');

CREATE TABLE Paiement_Adherent (
    id SERIAL PRIMARY KEY,
    id_adherent INT NOT NULL REFERENCES Adherent(id) ON DELETE CASCADE,
    id_type INT NOT NULL REFERENCES Type_Payement(id),
    date_paiement DATE NOT NULL,
    montant_paye NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    id_statut INT REFERENCES Statut_Paiement(id)
);

CREATE TABLE Historique_Statut_Paiement (
    id SERIAL PRIMARY KEY,
    id_paiement INT NOT NULL REFERENCES Paiement_Adherent(id) ON DELETE CASCADE,
    id_statut INT NOT NULL REFERENCES Statut_Paiement(id),
    id_admin INT NOT NULL REFERENCES Admin(id_admin),
    montant_paye NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT
);

CREATE TABLE Statut_Reservation (
    id SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE 
);

CREATE TABLE Reservation (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    id_exemplaire INT REFERENCES Exemplaire(id_exemplaire),
    date_created DATE NOT NULL ,
    date_reservation DATE NOT NULL , 
    is_validate BOOLEAN DEFAULT false  , 
    statut_id INT REFERENCES Statut_Reservation(id) DEFAULT 1  
);


INSERT INTO pret (id_adherent, id_exemplaire, date_pret, date_retour_prevue, date_retour_reelle, id_type_pret, est_prolonge, statut)
VALUES 
(23, 1, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', NULL, 1, false, 2);


