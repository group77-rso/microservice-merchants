INSERT INTO merchants (id, name, logourl) VALUES (1001, 'Mercator', 'https://www.mercatorgroup.si/assets/Logotip-lezec/mercator-logotip-positive-lezeci.png');
INSERT INTO merchants (id, name, logourl) VALUES (1002, 'Spar', 'https://upload.wikimedia.org/wikipedia/commons/7/7c/Spar-logo.svg');
INSERT INTO merchants (id, name, logourl) VALUES (1003, 'Jager', 'https://www.jager-prenosniki.si/templates/jager/assets/images/logo.png');

-- Mercator
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1001, 1.69, 'https://trgovina.mercator.si/market/izdelek/930519/trajno-polnomastno-alpsko-mleko-3-5-m-m-ljubljanske-mlekarne-1-l?fbclid=IwAR1v11hsK0ehZp_7benWbd4kb9a8ywmNW9HYkrocjmtEWqNyYCBVrNJ_bx0');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1002, 1.65, 'https://trgovina.mercator.si/market/izdelek/101595/trajno-polnomastno-mleko-3-5-m-m-pomurske-mlekarne-1-l');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1003, 1.99, 'https://trgovina.mercator.si/market/izdelek/19262830/rizev-napitek-bio-zone-1-l');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1005, 1.14, 'https://trgovina.mercator.si/market/izdelek/181663/posebna-psenicna-bela-moka-t-400-zito-1-kg');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1006, 3.79, 'https://trgovina.mercator.si/market/izdelek/11873032/moka-farina-brez-glutena-schar-1-kg');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1007, 2.89, 'https://trgovina.mercator.si/market/izdelek/16648849/pirina-moka-zito-1-kg');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1008, 2.35, 'https://trgovina.mercator.si/market/izdelek/17961253/jajca-l-hlevska-reja-mercator-10-1');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1009, 1.59, 'https://trgovina.mercator.si/market/izdelek/1233047/beli-kristalni-sladkor-agragold-1-kg');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1010, 1.84, 'https://trgovina.mercator.si/market/izdelek/13789528/mleti-sladkor-agragold-500-g');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1011, 2.24, 'https://trgovina.mercator.si/market/izdelek/11944256/flips-smoki-250-g');
INSERT INTO price (merchantId, productId, price, link) VALUES (1001, 1012, 0.72, 'https://trgovina.mercator.si/market/izdelek/39995/zvecilni-gumi-spearmint-orbit-14-g');

-- Spar
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1001, 1.49, 'https://www.spar.si/online/alpsko-mleko-polnomastno-35-mm-1l/p/266388');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1002, 1.58, 'https://www.spar.si/online/pomursko-mlejko-35-1l/p/47101');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1003, 1.19, 'https://www.spar.si/online/rizev-napitek-s-kalcijem-spar-veggie-1l/p/532562');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1005, 1.69, 'https://www.spar.si/online/posebna-bela-moka-zito-1kg/p/43424');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1006, 3.99, 'https://www.spar.si/online/moka-farina-brez-glutena-schr-1kg/p/502196');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1007, 2.78, 'https://www.spar.si/online/pirina-moka-zito-1kg/p/470990');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1008, 2.35, 'https://www.spar.si/online/sveza-jajca-hlevska-reja-l-s-budget-101/p/532511');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1009, 1.59, 'https://www.spar.si/online/beli-kristalni-sladkor-agragold-1kg/p/299157');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1010, 1.79, 'https://www.spar.si/online/mleti-sladkor-agragold-500g/p/414351');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1011, 2.17, 'https://www.spar.si/online/flips-z-arasidi-smoki-stark-250g/p/374184');
INSERT INTO price (merchantId, productId, price, link) VALUES (1002, 1012, 0.70, 'https://www.spar.si/online/bonboni-spear-mints-orbit-161-28g/p/414886');

-- Jager
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1001, 1.49, 'https://www.trgovinejager.com/mleko-sveze-in-trajno/mleko-alpsko-35-1l/');
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1002, 1.34, 'https://www.trgovinejager.com/mleko-sveze-in-trajno/mleko-pomursko-1l-pokrovcek-35-m.m./');
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1006, 3.59, 'https://www.trgovinejager.com/pomozna-zdr.-sredstva/moka-farina-1kg-brez-glutena_1/');
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1010, 0.99, 'https://www.trgovinejager.com/sladkor-beli-rjavi/sladkor-mleti-400g-jager_1/');
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1011, 1.74, 'https://www.trgovinejager.com/cips-flips-slani-program/smoki-250gr/');
INSERT INTO price (merchantId, productId, price, link) VALUES (1003, 1012, 0.69, 'https://www.trgovinejager.com/zvecilni-gumi/zvecilni-orbit-14g-spearmint/');
