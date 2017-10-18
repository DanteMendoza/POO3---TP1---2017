

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
  id_usuario1_FK int,
  id_usuario2_FK int,
  PRIMARY KEY (id_conversacion_PK)
--  CONSTRAINT fk_CONVERSACIONES_USUARIOS1
--    FOREIGN KEY (id_usuario1_FK , id_usuario2_FK)
--    REFERENCES usuarios (id_usuario_PK , id_usuario2_PK)
-- CONSTRAINT fk_CONVERSACIONES_USUARIOS2
--    FOREIGN KEY (id_usuario2_FK )
--    REFERENCES usuarios (id_usuario_PK )
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
  texto_mensaje TEXT NULL,
  PRIMARY KEY (id_mensaje_PK) 
);


-- -----------------------------------------------------
-- INSERTS "mensajes" Insercion de Datos.
-- -----------------------------------------------------
DELETE FROM mensajes;
INSERT INTO mensajes(id_mensaje_PK, id_conversacion, texto_mensaje)
VALUES (3000, 4000, '- Hola como estas? salimos este sabado para el cine?');
INSERT INTO mensajes(id_mensaje_PK, id_conversacion, texto_mensaje)
VALUES (3001, 4001, '- Queria decirte que lo del viernes se suspende, lo siento mucho');
INSERT INTO mensajes(id_mensaje_PK, id_conversacion, texto_mensaje)
VALUES (3002, 4002, '- La verdad no entendi nada de la charla orientativa, me pregunto que intentaron decir?');
INSERT INTO mensajes(id_mensaje_PK, id_conversacion, texto_mensaje)
VALUES (3003, 4003, '- ya tengo todo comprado para la fiesta!');
INSERT INTO mensajes(id_mensaje_PK, id_conversacion, texto_mensaje)
VALUES (3004, 4004, '- jajaja en serio? uy en un re lio te metiste!');


-- -----------------------------------------------------
-- CONSULTAS.
-- -----------------------------------------------------
SELECT * FROM usuarios;
SELECT * FROM conversaciones;
SELECT * FROM mensajes;



