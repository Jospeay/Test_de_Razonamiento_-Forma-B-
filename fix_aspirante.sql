-- ============================================================
-- Script de reparación para Test_Razonamiento_B
-- Columna tipocolegio (lowercase sin comillas = nombre real en PostgreSQL)
-- Ejecutar en pgAdmin ANTES de reiniciar la aplicación
-- ============================================================

-- 1. Ver columnas reales de la tabla aspirante
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'aspirante'
ORDER BY ordinal_position;

-- 2. Añadir la columna si no existe (con DEFAULT para filas existentes)
ALTER TABLE aspirante ADD COLUMN IF NOT EXISTS tipocolegio VARCHAR(20) DEFAULT 'PUBLICO';

-- 3. Reparar filas con NULL (las que causan ERROR! en OpenXava)
UPDATE aspirante
SET tipocolegio = 'PUBLICO'
WHERE tipocolegio IS NULL OR tipocolegio = '';

-- 4. Normalizar a mayúsculas
UPDATE aspirante
SET tipocolegio = UPPER(tipocolegio)
WHERE tipocolegio IS NOT NULL;

-- 5. Reparar otros campos que podrían tener NULL
UPDATE aspirante SET activo = true WHERE activo IS NULL;
UPDATE aspirante SET "fechaRegistro" = CURRENT_DATE WHERE "fechaRegistro" IS NULL;

-- 6. Verificar resultado
SELECT id, codigo, "nombreCompleto", cedula, tipocolegio, activo, "fechaRegistro"
FROM aspirante;
