

-- -----------------------------------------------------
-- Table usuarios
-- -----------------------------------------------------
--DROP TABLE IF EXISTS usuarios;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario_PK bigserial NOT NULL,
  nombre_usuario  VARCHAR NULL,
  PRIMARY KEY (id_usuario_PK) 
);


-- -----------------------------------------------------
-- INSERTS "usuarios" Insercion de Datos.
-- -----------------------------------------------------
DELETE FROM usuarios;
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1000, 'Pedro');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1001, 'Donovan');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1002, 'Claudia');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1003, 'Camila');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1004, 'Mabel');
INSERT INTO usuarios(id_usuario_PK, nombre_usuario)
VALUES (1005, 'Juan');


-- -----------------------------------------------------
-- CONSULTAS.
-- -----------------------------------------------------
SELECT * FROM usuarios;



