-- Base de données
CREATE DATABASE biblio;
\c biblio;

-- Réinitialisation de séquence
ALTER SEQUENCE statut_pret_id_statut_pret_seq RESTART WITH 1;

-- Tables

Table Admin {
  id_admin serial [pk]
  nom_utilisateur varchar(100) [not null]
  mot_de_passe text [not null]
}

Table Type_Adherent {
  id serial [pk]
  nom_type varchar(50) [not null]
  quota_max_pret int [not null]
  duree_max_pret int [not null]
  quota_max_prolongement int [not null]
  duree_max_prolongement int [not null]
  created_at timestamp [default: current_timestamp]
  updated_at timestamp [default: current_timestamp]
}

Table Adherent {
  id serial [pk]
  nom varchar(100) [not null]
  prenom varchar(100)
  password varchar(200)
  date_naissance date [not null]
  date_inscription date [not null]
  id_type int [ref: > Type_Adherent.id]
  est_abonne boolean [default: false]
  est_penalise boolean [default: false]
}

Table Livre {
  id serial [pk]
  titre varchar(255) [not null]
  auteur varchar(100)
  isbn varchar(13)
  date_publication date
  age_restriction int
  nbr_exmp int [default: 1]
  description text
  image_url varchar(255)
  created_at timestamp [default: current_timestamp]
  updated_at timestamp [default: current_timestamp]
}

Table Type_Utilisation {
  id serial [pk]
  nom_type_utilisation varchar(50) [not null, unique]
}

Table Droit_Pret {
  id serial [pk]
  id_type_adherent int [ref: > Type_Adherent.id]
  id_type_utilisation int [ref: > Type_Utilisation.id]
  id_livre int [ref: > Livre.id]
  autorise boolean [default: false]
}

Table Statut_Pret {
  id_statut_pret serial [pk]
  nom_statut varchar(50) [not null, unique]
}

Table Exemplaire {
  id_exemplaire serial [pk]
  id_livre int [ref: > Livre.id]
  code_exemplaire varchar(50) [not null, unique]
  created_at timestamp [default: current_timestamp]
  updated_at timestamp [default: current_timestamp]
  id_status int [ref: > Statut_Pret.id_statut_pret, default: 1]
}

Table Type_Pret {
  id_type_pret serial [pk]
  nom_type_pret varchar(50)
  duree_max int
}

Table Pret {
  id serial [pk]
  id_adherent int [ref: > Adherent.id]
  id_exemplaire int [ref: > Exemplaire.id_exemplaire]
  date_pret date [not null]
  date_retour_prevue date [not null]
  date_retour_reelle date
  id_type_pret int [ref: > Type_Pret.id_type_pret]
  est_prolonge boolean [default: false]
  statut int [ref: > Statut_Pret.id_statut_pret]
}

Table Prolongement_Pret {
  id serial [pk]
  id_pret int [ref: > Pret.id]
  date_prolongement date [not null]
  date_retour_prevue date [not null]
  statut int [ref: > Statut_Pret.id_statut_pret]
}

Table Historique_Statut_Pret {
  id_historique serial [pk]
  id_pret int [ref: > Pret.id, on_delete: cascade]
  id_statut_pret int [ref: > Statut_Pret.id_statut_pret]
  id_admin int [ref: > Admin.id_admin]
  date_changement timestamp [default: current_timestamp]
}

Table Type_Penalite {
  id_type_penalite serial [pk]
  nom_type_penalite varchar(100)
  durre_penalite int
  description text
}

Table Penalite {
  id serial [pk]
  id_adherent int [ref: > Adherent.id]
  raison varchar(250)
  id_type_penalite int [ref: > Type_Penalite.id_type_penalite]
  date_debut date [not null]
  date_fin date [not null]
}

Table Type_Payement {
  id serial [pk]
  type_name varchar(250)
  montant_payement int
}

Table Statut_Paiement {
  id serial [pk]
  nom_statut varchar(50) [not null, unique]
}

Table Paiement_Adherent {
  id serial [pk]
  id_adherent int [ref: > Adherent.id, on_delete: cascade]
  id_type int [ref: > Type_Payement.id]
  date_paiement date [not null]
  montant_paye numeric(10,2) [default: 0.00, not null]
  id_statut int [ref: > Statut_Paiement.id]
}

Table Historique_Statut_Paiement {
  id serial [pk]
  id_paiement int [ref: > Paiement_Adherent.id, on_delete: cascade]
  id_statut int [ref: > Statut_Paiement.id]
  id_admin int [ref: > Admin.id_admin]
  montant_paye numeric(10,2) [default: 0.00, not null]
  date_changement timestamp [default: current_timestamp]
  commentaire text
}

Table Statut_Reservation {
  id serial [pk]
  nom_statut varchar(50) [not null, unique]
}

Table Reservation {
  id serial [pk]
  id_adherent int [ref: > Adherent.id]
  id_exemplaire int [ref: > Exemplaire.id_exemplaire]
  date_created date [not null]
  date_reservation date [not null]
  is_validate boolean [default: false]
  statut_id int [ref: > Statut_Reservation.id, default: 1]
}

-- Données initiales
INSERT INTO Statut_Paiement (nom_statut) VALUES ('en_attente'), ('valide'), ('rejeté');

INSERT INTO Pret (
  id_adherent, id_exemplaire, date_pret, date_retour_prevue,
  date_retour_reelle, id_type_pret, est_prolonge, statut
) VALUES (
  23, 1, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days',
  NULL, 1, false, 2
);
