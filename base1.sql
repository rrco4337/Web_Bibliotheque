-- Création de la base
CREATE DATABASE biblio;
\c biblio;
ALTER SEQUENCE statut_pret_id_statut_pret_seq RESTART WITH 1;

-- Table des administrateurs
CREATE TABLE Admin (
    id_admin SERIAL PRIMARY KEY,
    nom_utilisateur VARCHAR(100) NOT NULL,
    mot_de_passe TEXT NOT NULL
);

-- Table des types d’adhérents
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

-- Table des adhérents
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

select * 
    from  Adherent as ad
    join Type_Adherent as ta on ad.id_type = ta.id
    where ad.id = 1;

-- Table des livres
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
-- alter table Livre add column  nbr_exmp int DEFAULT 1 ; 

alter table Livre add column  image_url VARCHAR(255);
-- alter table Livre add column  description TEXT;
-- Table des types d'utilisation des exemplaires
CREATE TABLE Type_Utilisation (
    id SERIAL PRIMARY KEY,
    nom_type_utilisation VARCHAR(50) NOT NULL UNIQUE
);

-- Droits de prêt par type d’adhérent et type d’utilisation
CREATE TABLE Droit_Pret (
    id SERIAL PRIMARY KEY,
    id_type_adherent INT REFERENCES Type_Adherent(id),
    id_type_utilisation INT REFERENCES Type_Utilisation(id),
    id_livre INT REFERENCES Livre(id),
    autorise BOOLEAN DEFAULT FALSE
);

-- Table des statuts de prêt
CREATE TABLE Statut_Pret (
    id_statut_pret SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE
);
insert into Statut_Pret (nom_statut) values ('disponible'), ('prêté'), ('en réparation'), ('perdu');


-- Table des exemplaires de livres
CREATE TABLE Exemplaire (
    id_exemplaire SERIAL PRIMARY KEY,
    id_livre INT REFERENCES Livre(id),
    code_exemplaire VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_status INT REFERENCES Statut_Pret(id_statut_pret) DEFAULT 1, -- 1: disponible, 2: prêté, 3: en réparation, 4: perdu
);

SELECT a FROM Exemplaire a JOIN FETCH a.Livre WHERE a.id = :id
SELECT * 
    FROM  Exemplaire AS ex 
    join Livre as lv on lv.id = ex.id_livre 


select * 
    from Exemplaire as ex
    join Livre as l on ex.id_livre = l.id
    join Statut_Pret as sp on ex.id_status = sp.id_statut_pret
    where l.id = 10;


-- Table des types de prêt
CREATE TABLE Type_Pret (
    id_type_pret SERIAL PRIMARY KEY,
    nom_type_pret VARCHAR(50),
    duree_max INT
);
insert into Type_Pret (nom_type_pret, duree_max) values  ('Lecture sur place', 0) ,  ('Pret a domicile', 0); 



    -- Table des prêts
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

-- Historique des statuts de prêt
CREATE TABLE Historique_Statut_Pret (
    id_historique SERIAL PRIMARY KEY,
    id_pret INT NOT NULL REFERENCES Pret(id) ON DELETE CASCADE,
    id_statut_pret INT NOT NULL REFERENCES Statut_Pret(id_statut_pret),
    id_admin INT NOT NULL REFERENCES Admin(id_admin),
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des types de pénalités
CREATE TABLE Type_Penalite (
    id_type_penalite SERIAL PRIMARY KEY,
    nom_type_penalite VARCHAR(100),
    durre_penalite INT, -- en jour 
    description TEXT
);

-- Table des pénalités infligées aux adhérents
CREATE TABLE Penalite (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    raison VARCHAR(250), 
    id_type_penalite INT REFERENCES Type_Penalite(id_type_penalite),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL
);

-- Table des types de paiement
CREATE TABLE Type_Payement (
    id SERIAL PRIMARY KEY,
    type_name VARCHAR(250), -- cotisation
    montant_payement INT,
    durre_validite INT DEFAULT 0 -- en jour 
);

-- Table des statuts de paiement
CREATE TABLE Statut_Paiement (
    id SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE -- ex: en_attente, valide, rejeté
);
insert into  Statut_Paiement (nom_statut) values ('en_attente'), ('valide'), ('rejeté');

-- Table des paiements des adhérents
CREATE TABLE Paiement_Adherent (
    id SERIAL PRIMARY KEY,
    id_adherent INT NOT NULL REFERENCES Adherent(id) ON DELETE CASCADE,
    id_type INT NOT NULL REFERENCES Type_Payement(id),
    date_paiement DATE NOT NULL,
    montant_paye NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    id_statut INT REFERENCES Statut_Paiement(id)
);

-- Historique des statuts de paiement
CREATE TABLE Historique_Statut_Paiement (
    id SERIAL PRIMARY KEY,
    id_paiement INT NOT NULL REFERENCES Paiement_Adherent(id) ON DELETE CASCADE,
    id_statut INT NOT NULL REFERENCES Statut_Paiement(id),
    id_admin INT NOT NULL REFERENCES Admin(id_admin),
    montant_paye NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT
);

-- Table des statuts de paiement
CREATE TABLE Statut_Reservation (
    id SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE -- ex: en_attente, valide, rejeté
);

-- Table Reservation
CREATE TABLE Reservation (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    id_exemplaire INT REFERENCES Exemplaire(id_exemplaire),
    date_created DATE NOT NULL ,
    date_reservation DATE NOT NULL , 
    is_validate BOOLEAN DEFAULT false  , 
    statut_id INT REFERENCES Statut_Reservation(id) DEFAULT 1  
);





