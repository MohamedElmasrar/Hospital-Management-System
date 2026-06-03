-- Demo billing setup. Prices are sample amounts for development.
INSERT INTO cash_desks (id, desk_code, department_id, section, is_active, date_created, date_modified)
SELECT '30000000-0000-0000-0000-000000000001', 'DEMO-BILLING', '11000000-0000-0000-0000-000000000001', 'RECEPTION', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_desks WHERE desk_code = 'DEMO-BILLING');

INSERT INTO service_catalog (id, service_code, department_id, name, is_active, date_created, date_modified)
SELECT '31000000-0000-0000-0000-000000000001', 'DEMO-CONSULTATION', '11000000-0000-0000-0000-000000000001', 'Consultation (demo)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM service_catalog WHERE service_code = 'DEMO-CONSULTATION');

INSERT INTO service_tariff_versions (id, catalog_item_id, unit_price, tax_rate, effective_from, date_created)
SELECT '32000000-0000-0000-0000-000000000001', id, 200.00, 0, '2020-01-01', CURRENT_TIMESTAMP
FROM service_catalog WHERE service_code = 'DEMO-CONSULTATION'
AND NOT EXISTS (SELECT 1 FROM service_tariff_versions WHERE catalog_item_id = service_catalog.id AND effective_to IS NULL);
