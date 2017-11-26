

-- -----------------------------------------------------
-- Table usuarios
-- -----------------------------------------------------
--DROP TABLE IF EXISTS usuarios;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario_PK int NOT NULL,
  nombre_usuario VARCHAR NULL,
  password_usuario VARCHAR NOT NULL,
  PRIMARY KEY (id_usuario_PK) 
);


-- -----------------------------------------------------
-- INSERTS "usuarios" Insercion de Datos.
-- -----------------------------------------------------
DELETE FROM usuarios;
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1000, 'Pedro', '1234');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1001, 'Donovan', '1234');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1002, 'Claudia', '1234');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1003, 'Camila', '1234');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1004, 'Mabel', '1234');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario)
VALUES (1005, 'Juan', '1234');


-- -----------------------------------------------------
-- Table conversaciones
-- -----------------------------------------------------
--DROP TABLE IF EXISTS conversaciones;

CREATE TABLE IF NOT EXISTS conversaciones (
  id_conversacion_PK int NOT NULL,
  id_usuario1_FK int UNIQUE,
  id_usuario2_FK int UNIQUE,
  PRIMARY KEY (id_conversacion_PK),
  CONSTRAINT fk_CONVERSACIONES_USUARIOS1
    FOREIGN KEY (id_usuario1_FK)
    REFERENCES usuarios (id_usuario_PK),
  CONSTRAINT fk_CONVERSACIONES_USUARIOS2
    FOREIGN KEY (id_usuario2_FK)
    REFERENCES usuarios (id_usuario_PK)
);


-- -----------------------------------------------------
-- INSERTS "conversaciones" Insercion de Datos.
-- -----------------------------------------------------
DELETE FROM conversaciones;
INSERT INTO conversaciones(id_conversacion_PK, id_usuario1_FK, id_usuario2_FK)
VALUES (2000, 1000, 1001);
INSERT INTO conversaciones(id_conversacion_PK, id_usuario1_FK, id_usuario2_FK)
VALUES (2001, 1002, 1003);
INSERT INTO conversaciones(id_conversacion_PK, id_usuario1_FK, id_usuario2_FK)
VALUES (2002, 1004, 1005);
INSERT INTO conversaciones(id_conversacion_PK, id_usuario1_FK, id_usuario2_FK)
VALUES (2003, 1000, 1005);
INSERT INTO conversaciones(id_conversacion_PK, id_usuario1_FK, id_usuario2_FK)
VALUES (2004, 1000, 1001);




-- -----------------------------------------------------
-- Table mensajes
-- -----------------------------------------------------
--DROP TABLE IF EXISTS mensajes;

CREATE TABLE IF NOT EXISTS mensajes (
  id_mensaje_PK int NOT NULL,
  id_conversacion int NULL,
  emisor int NOT NULL,
  texto_mensaje TEXT NULL,
  leido TEXT NOT NULL,
  PRIMARY KEY (id_mensaje_PK) 
);




-- -----------------------------------------------------
-- CONSULTAS.
-- -----------------------------------------------------
DELETE FROM mensajes;
DELETE FROM conversaciones;
DELETE FROM usuarios;


SELECT * FROM usuarios;
SELECT * FROM conversaciones;
SELECT * FROM mensajes;









