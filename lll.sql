
Table Type_Adherent {
  id serial [pk]
  nom_type varchar(50) [not null]
  quota_max_pret int [not null]
  duree_max_pret int [not null]
  quota_max_prolongement int [not null]
  duree_max_prolongement int [not null]
  created_at timestamp 
  updated_at timestamp 
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
  created_at timestamp 
  updated_at timestamp 
}





Table Statut_Pret {
  id_statut_pret serial [pk]
  nom_statut varchar(50) [not null, unique]
}

Table Exemplaire {
  id_exemplaire serial [pk]
  id_livre int [ref: > Livre.id]
  code_exemplaire varchar(50) [not null, unique]
  created_at timestamp 
  updated_at timestamp
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