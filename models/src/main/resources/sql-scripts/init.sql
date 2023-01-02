INSERT INTO merchants (id, name, logourl) VALUES (1, 'Mercator', 'https://www.mercatorgroup.si/assets/Logotip-lezec/mercator-logotip-positive-lezeci.png');
INSERT INTO merchants (id, name, logourl) VALUES (2, 'Spar', 'https://upload.wikimedia.org/wikipedia/commons/7/7c/Spar-logo.svg');
INSERT INTO merchants (id, name, logourl) VALUES (3, 'Jager', 'https://www.jager-prenosniki.si/templates/jager/assets/images/logo.png');


INSERT INTO price (merchantId, productId, price) VALUES (1, 1, 1.32);
INSERT INTO price (merchantId, productId, price) VALUES (1, 2, 1.19);
INSERT INTO price (merchantId, productId, price) VALUES (1, 3, 1.19);
INSERT INTO price (merchantId, productId, price) VALUES (2, 1, 1.49);
INSERT INTO price (merchantId, productId, price) VALUES (2, 2, 1.09);