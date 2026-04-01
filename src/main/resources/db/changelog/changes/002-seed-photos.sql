--liquibase formatted sql

--changeset codex:002-seed-photos
insert into photos (
    id, title, district, latitude, longitude, created_at,
    ai_description, caption, ai_confidence, source, image_url, status
)
values
    (
        'mtatsminda-sun',
        'Рыжий пес у смотровой',
        'Мтацминда',
        41.6951,
        44.7863,
        '2026-03-29T18:10:00Z',
        'Рыжая собака спокойно сидит у обзорной площадки, смотрит в сторону города и выглядит привыкшей к людям.',
        'Похоже, местный любимец у фуникулера.',
        0.96,
        'TELEGRAM',
        'https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=1200&q=80',
        'PUBLISHED'
    ),
    (
        'vera-green',
        'Белая собака в тени деревьев',
        'Вера',
        41.7102,
        44.7796,
        '2026-03-28T09:24:00Z',
        'Светлая собака лежит у зеленой аллеи, вокруг тихий двор и мягкий утренний свет.',
        'Очень спокойная, не реагировала на прохожих.',
        0.93,
        'TELEGRAM',
        'https://images.unsplash.com/photo-1518717758536-85ae29035b6d?auto=format&fit=crop&w=1200&q=80',
        'PUBLISHED'
    ),
    (
        'avlabari-stairs',
        'Черный пес на лестнице',
        'Авлабари',
        41.6926,
        44.8127,
        '2026-03-27T14:45:00Z',
        'Черная собака стоит на каменной лестнице, поза настороженная, рядом старая городская застройка.',
        'Снято возле узкой улочки с видом на старый город.',
        0.91,
        'TELEGRAM',
        'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=1200&q=80',
        'PUBLISHED'
    ),
    (
        'saburtalo-park',
        'Пес после дождя',
        'Сабуртало',
        41.7273,
        44.7584,
        '2026-03-26T16:05:00Z',
        'Собака среднего размера идет по влажной дорожке парка, шерсть темная, вокруг низкая весенняя зелень.',
        'После короткого дождя в парке.',
        0.89,
        'TELEGRAM',
        'https://images.unsplash.com/photo-1518020382113-a7e8fc38eac9?auto=format&fit=crop&w=1200&q=80',
        'PUBLISHED'
    )
on conflict (id) do nothing;
