-- Création de la base
CREATE DATABASE bibliotheque;
\c bibliotheque;

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
    password varchar(200) , 
    date_naissance DATE NOT NULL,
    date_inscription DATE NOT NULL,
    id_type INT REFERENCES Type_Adherent(id),
    est_abonne BOOLEAN DEFAULT FALSE,
    est_penalise BOOLEAN DEFAULT FALSE
);

-- Table des livres
CREATE TABLE Livre (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(100),
    isbn VARCHAR(13),
    date_publication DATE,
    age_restriction int ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des types d'utilisation des exemplaires (A emporter  ,  a lire sur place)
CREATE TABLE Type_Utilisation (
    id SERIAL PRIMARY KEY,
    nom_type_utilisation VARCHAR(50) NOT NULL UNIQUE
);

-- Droits de prêt par type d’adhérent et type d’utilisation (Droit sur chaque adherant)
CREATE TABLE Droit_Pret (
    id SERIAL PRIMARY KEY,
    id_type_adherent INT REFERENCES Type_Adherent(id),
    id_type_utilisation INT REFERENCES Type_Utilisation(id),
    id_livre INT REFERENCES Livre(id),
    autorise BOOLEAN DEFAULT FALSE
);

-- Table des exemplaires de livres
CREATE TABLE Exemplaire (
    id_exemplaire SERIAL PRIMARY KEY,
    id_livre INT REFERENCES Livre(id),
    code_exemplaire VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des types de prêt
CREATE TABLE Type_Pret (
    id_type_pret SERIAL PRIMARY KEY,
    nom_type_pret VARCHAR(50),
    duree_max INT
);

-- Table des statuts de prêt
CREATE TABLE Statut_Pret (
    id_statut_pret SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE
);

-- Table des prêts
CREATE TABLE Pret (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    id_exemplaire INT REFERENCES Exemplaire(id_exemplaire),
    date_pret DATE NOT NULL,
    date_retour_prevue DATE NOT NULL,
    date_retour_reelle DATE,
    id_type_pret INT REFERENCES Type_Pret(id_type_pret),
    est_prolonge int DEFAULT 0  , 
    statut INT REFERENCES Statut_Pret(id_statut_pret)
); 



create table Reservation (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    id_exemplaire INT REFERENCES Exemplaire(id_exemplaire),
    date_reservation DATE NOT NULL,
    sa
) ; 

-- Table des prolongement prêts
CREATE TABLE Prolongement_Pret (
    id SERIAL PRIMARY KEY ,
    id_pret INT REFERENCES Pret(id) ,
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
    durre_penalite int  , -- en jour 
    description TEXT
);

-- Table des pénalités infligées aux adhérents
CREATE TABLE Penalite (
    id SERIAL PRIMARY KEY,
    id_adherent INT REFERENCES Adherent(id),
    raison varchar(250) , 
    id_type_penalite INT REFERENCES Type_Penalite(id_type_penalite),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL
);

create table Type_Payement (
    id SERIAL PRIMARY KEY,
    type_name varchar(250) , -- cotisation
    montant_payement int ,
    durre_validite int DEFAULT 0 -- en jour 
);

CREATE TABLE Statut_Paiement (
    id SERIAL PRIMARY KEY,
    nom_statut VARCHAR(50) NOT NULL UNIQUE  -- ex: en_attente, valide, rejeté
);

CREATE TABLE Paiement_Adherent (
    id SERIAL PRIMARY KEY,
    id_adherent INT NOT NULL REFERENCES Adherent(id) ON DELETE CASCADE,
    id_type INT NOT NULL REFERENCES Type_Paiement(id) ON DELETE SET NULL,
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




-- Insertion des administrateurs
INSERT INTO Admin (nom_utilisateur, mot_de_passe) VALUES 
('admin1', 'password1'),
('admin2', 'password2'),
('admin3', 'password3');

-- Insertion des types d'adhérents
INSERT INTO Type_Adherent (nom_type, quota_max_pret, duree_max_pret, quota_max_prolongement, duree_max_prolongement) VALUES 
('Enfant', 3, 14, 1, 7),
('Adulte', 5, 21, 2, 14),
('Etudiant', 7, 28, 2, 14),
('Senior', 6, 28, 2, 14);

-- Insertion des adhérents
INSERT INTO Adherent (nom, prenom, password, date_naissance, date_inscription, id_type, est_abonne, est_penalise) VALUES 
('Dupont', 'Jean', 'pass123', '1990-05-15', '2023-01-10', 2, TRUE, FALSE),
('Martin', 'Sophie', 'pass456', '1985-08-22', '2023-02-15', 2, TRUE, FALSE),
('Bernard', 'Luc', 'pass789', '1978-11-30', '2023-03-20', 3, TRUE, FALSE),
('Petit', 'Emma', 'pass101', '2012-04-05', '2023-04-25', 1, TRUE, FALSE),
('Moreau', 'Pierre', 'pass202', '1960-07-18', '2023-05-30', 4, TRUE, FALSE),
('Lefebvre', 'Camille', 'pass303', '1995-09-12', '2023-06-05', 3, TRUE, FALSE),
('Roux', 'Thomas', 'pass404', '2010-12-24', '2023-07-10', 1, FALSE, TRUE),
('Fournier', 'Laura', 'pass505', '1982-03-08', '2023-08-15', 2, TRUE, FALSE),
('Girard', 'Nicolas', 'pass606', '1975-06-19', '2023-09-20', 4, TRUE, FALSE),
('Mercier', 'Alice', 'pass707', '2008-10-31', '2023-10-25', 1, FALSE, FALSE);

-- Insertion des livres
INSERT INTO Livre (titre, auteur, isbn, date_publication, age_restriction, nbr_exmp, description, image_url) VALUES 
('Le Petit Prince', 'Antoine de Saint-Exupéry', '9782070408504', '1943-04-06', 6, 5, 'Un conte poétique et philosophique', 'petit_prince.jpg'),
('1984', 'George Orwell', '9782070368228', '1949-06-08', 16, 3, 'Roman dystopique', '1984.jpg'),
('Harry Potter à lécole des sorciers', 'J.K. Rowling', '9782070518425', '1997-06-26', 10, 4, 'Premier tome de la saga Harry Potter', 'harry_potter.jpg'),
('Le Seigneur des Anneaux', 'J.R.R. Tolkien', '9782266282360', '1954-07-29', 12, 2, 'Trilogie fantastique', 'seigneur_anneaux.jpg'),
('Gatsby le Magnifique', 'F. Scott Fitzgerald', '9782070376926', '1925-04-10', 14, 3, 'Roman sur Amérique des années 20', 'gatsby.jpg'),
('Orgueil et Préjugés', 'Jane Austen', '9782253006329', '1813-01-28', 12, 2, 'Roman classique anglais', 'orgueil_prejuges.jpg'),
('Le Comte de Monte-Cristo', 'Alexandre Dumas', '9782253088530', '1844-08-28', 14, 1, 'Roman aventure et de vengeance', 'monte_cristo.jpg'),
('Crime et Châtiment', 'Fiodor Dostoïevski', '9782070413140', '1866-01-01', 16, 2, 'Roman psychologique', 'crime_chatiment.jpg'),
('Étranger', 'Albert Camus', '9782070360024', '1942-05-19', 14, 3, 'Roman existentialiste', 'etranger.jpg'),
('Voyage au bout de la nuit', 'Louis-Ferdinand Céline', '9782070360284', '1932-10-15', 18, 1, 'Roman sombre et pessimiste', 'voyage_nuit.jpg');

-- Insertion des types d'utilisation
INSERT INTO Type_Utilisation (nom_type_utilisation) VALUES 
('Lecture sur place'),
('Prêt à domicile'),
('Consultation rapide'),
('Prêt spécial');

-- Insertion des droits de prêt
INSERT INTO Droit_Pret (id_type_adherent, id_type_utilisation, id_livre, autorise) VALUES 
(1, 1, 1, TRUE),
(1, 2, 1, TRUE),
(2, 1, 1, TRUE),
(2, 2, 1, TRUE),
(3, 1, 1, TRUE),
(3, 2, 1, TRUE),
(4, 1, 1, TRUE),
(4, 2, 1, TRUE),
(1, 1, 2, TRUE),
(1, 2, 2, FALSE),
(2, 1, 2, TRUE),
(2, 2, 2, TRUE),
(3, 1, 2, TRUE),
(3, 2, 2, TRUE),
(4, 1, 2, TRUE),
(4, 2, 2, TRUE);

-- Insertion des exemplaires
INSERT INTO Exemplaire (id_livre, code_exemplaire, id_status) VALUES 
(1, 'EXP-001-001', 1),
(1, 'EXP-001-002', 1),
(1, 'EXP-001-003', 1),
(1, 'EXP-001-004', 1),
(1, 'EXP-001-005', 1),
(2, 'EXP-002-001', 1),
(2, 'EXP-002-002', 1),
(2, 'EXP-002-003', 1),
(3, 'EXP-003-001', 1),
(3, 'EXP-003-002', 1),
(3, 'EXP-003-003', 1),
(3, 'EXP-003-004', 1),
(4, 'EXP-004-001', 1),
(4, 'EXP-004-002', 1),
(5, 'EXP-005-001', 1),
(5, 'EXP-005-002', 1),
(5, 'EXP-005-003', 1),
(6, 'EXP-006-001', 1),
(6, 'EXP-006-002', 1),
(7, 'EXP-007-001', 1),
(8, 'EXP-008-001', 1),
(8, 'EXP-008-002', 1),
(9, 'EXP-009-001', 1),
(9, 'EXP-009-002', 1),
(9, 'EXP-009-003', 1),
(10, 'EXP-010-001', 1);

-- Insertion des types de pénalités
INSERT INTO Type_Penalite (nom_type_penalite, durre_penalite, description) VALUES 
('Retard mineur', 7, 'Retard de moins de 7 jours'),
('Retard majeur', 14, 'Retard de plus de 7 jours'),
('Livre endommagé', 30, 'Livre rendu en mauvais état'),
('Livre perdu', 60, 'Livre non rendu après plusieurs rappels');

-- Insertion des pénalités
INSERT INTO Penalite (id_adherent, raison, id_type_penalite, date_debut, date_fin) VALUES 
(7, 'Retard de 10 jours sur le retour d un livre', 2, '2023-09-01', '2023-09-15'),
(10, 'Livre légèrement abîmé', 3, '2023-10-05', '2023-11-04');

-- Insertion des types de paiement
INSERT INTO Type_Payement (type_name, montant_payement, durre_validite) VALUES 
('Cotisation annuelle enfant', 15, 365),
('Cotisation annuelle adulte', 30, 365),
('Cotisation annuelle étudiant', 20, 365),
('Cotisation annuelle senior', 25, 365),
('Amende retard', 5, 0),
('Remplacement livre', 20, 0);

-- Insertion des paiements
INSERT INTO Paiement_Adherent (id_adherent, id_type, date_paiement, montant_paye, id_statut) VALUES 
(1, 2, '2023-01-10', 30.00, 2),
(2, 2, '2023-02-15', 30.00, 2),
(3, 3, '2023-03-20', 20.00, 2),
(4, 1, '2023-04-25', 15.00, 2),
(5, 4, '2023-05-30', 25.00, 2),
(6, 3, '2023-06-05', 20.00, 2),
(7, 1, '2023-07-10', 15.00, 1),
(8, 2, '2023-08-15', 30.00, 2),
(9, 4, '2023-09-20', 25.00, 2),
(10, 1, '2023-10-25', 15.00, 1),
(7, 5, '2023-09-01', 5.00, 2),
(10, 5, '2023-10-05', 5.00, 2);

-- Insertion des statuts de réservation
INSERT INTO Statut_Reservation (nom_statut) VALUES 
('En attente'),
('Confirmée'),
('Annulée'),
('Expirée');

-- Insertion des réservations
INSERT INTO Reservation (id_adherent, id_exemplaire, date_created, date_reservation, is_validate, statut_id) VALUES 
(1, 1, '2023-11-01', '2023-11-10', TRUE, 2),
(2, 6, '2023-11-02', '2023-11-11', FALSE, 1),
(3, 9, '2023-11-03', '2023-11-12', TRUE, 2),
(4, 13, '2023-11-04', '2023-11-13', FALSE, 1);

-- Insertion des prêts
INSERT INTO Pret (id_adherent, id_exemplaire, date_pret, date_retour_prevue, date_retour_reelle, id_type_pret, est_prolonge, statut) VALUES 
(1, 1, '2023-11-01', '2023-11-15', NULL, 2, FALSE, 2),
(2, 6, '2023-11-02', '2023-11-16', NULL, 2, FALSE, 2),
(3, 9, '2023-11-03', '2023-11-17', NULL, 2, FALSE, 2),
(4, 13, '2023-11-04', '2023-11-18', NULL, 2, FALSE, 2),
(5, 15, '2023-10-20', '2023-11-10', '2023-11-09', 2, FALSE, 1),
(6, 18, '2023-10-25', '2023-11-15', NULL, 2, TRUE, 2);

-- Mise à jour des exemplaires prêtés
UPDATE Exemplaire SET id_status = 2 WHERE id_exemplaire IN (1, 6, 9, 13, 15, 18);

-- Insertion des prolongements de prêts
INSERT INTO Prolongement_Pret (id_pret, date_prolongement, date_retour_prevue, statut) VALUES 
(6, '2023-11-10', '2023-11-22', 2);

-- Insertion de l'historique des statuts de prêt
INSERT INTO Historique_Statut_Pret (id_pret, id_statut_pret, id_admin, date_changement) VALUES 
(1, 2, 1, '2023-11-01 10:00:00'),
(2, 2, 1, '2023-11-02 11:00:00'),
(3, 2, 2, '2023-11-03 12:00:00'),
(4, 2, 2, '2023-11-04 13:00:00'),
(5, 2, 3, '2023-10-20 14:00:00'),
(5, 1, 3, '2023-11-09 15:00:00'),
(6, 2, 1, '2023-10-25 16:00:00');

-- Insertion de l'historique des statuts de paiement
INSERT INTO Historique_Statut_Paiement (id_paiement, id_statut, id_admin, montant_paye, date_changement, commentaire) VALUES 
(1, 2, 1, 30.00, '2023-01-10 10:00:00', 'Paiement reçu'),
(2, 2, 1, 30.00, '2023-02-15 11:00:00', 'Paiement reçu'),
(3, 2, 2, 20.00, '2023-03-20 12:00:00', 'Paiement reçu'),
(4, 2, 2, 15.00, '2023-04-25 13:00:00', 'Paiement reçu'),
(5, 2, 3, 25.00, '2023-05-30 14:00:00', 'Paiement reçu'),
(6, 2, 3, 20.00, '2023-06-05 15:00:00', 'Paiement reçu'),
(7, 1, 1, 15.00, '2023-07-10 16:00:00', 'Paiement en attente de validation'),
(8, 2, 2, 30.00, '2023-08-15 17:00:00', 'Paiement reçu'),
(9, 2, 3, 25.00, '2023-09-20 18:00:00', 'Paiement reçu'),
(10, 1, 1, 15.00, '2023-10-25 19:00:00', 'Paiement en attente de validation'),
(11, 2, 2, 5.00, '2023-09-01 20:00:00', 'Amende payée'),
(12, 2, 3, 5.00, '2023-10-05 21:00:00', 'Amende payée');