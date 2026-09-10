-- Rastreabilidade espacial do apontamento de colheita: onde, em campo, o
-- registro foi feito.
--
-- Nuláveis de propósito. O aparelho pode estar sem sinal de GPS, sob mata
-- fechada ou com a permissão negada, e nada disso pode impedir o agricultor de
-- registrar a produção — o apontamento entra sem coordenada.
--
-- NUMERIC(9,6) cobre a faixa válida de latitude (-90 a 90) e longitude
-- (-180 a 180) com seis casas decimais, cerca de 11 cm no equador. É mais
-- precisão do que o GPS de celular entrega e mais do que a delimitação de um
-- talhão exige.
ALTER TABLE production_executions
    ADD COLUMN latitude NUMERIC(9, 6),
    ADD COLUMN longitude NUMERIC(9, 6);

ALTER TABLE production_executions
    ADD CONSTRAINT chk_execution_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_execution_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
