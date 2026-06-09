# Картинки техники упражнений — как добавить

Приложение **само подхватывает** картинку, если в `app/src/main/res/drawable/`
лежит файл с именем `ex_<id>` (например `ex_pushup.webp`). Если файла нет —
показывается нарисованный силуэт (как сейчас). Никакого кода менять не нужно.

## Технические требования к файлам
- **Имя файла:** строго `ex_<id>` из таблицы ниже, только строчные буквы/цифры/`_`.
- **Формат:** `.webp` (рекомендуется, легче) или `.png`. Для прозрачного фона — PNG/WebP с альфа-каналом.
- **Размер:** квадрат, **1024×1024** (или 1024×768). Одинаковый для всех — будет ровно.
- **Фон:** прозрачный **или** тёмный `#141925` (тема приложения тёмная).
- **Без текста на картинке** — подписи рисует само приложение.
- Куда класть: `app/src/main/res/drawable/ex_pushup.webp` и т.д. Пересобрать — и всё.

> Видео/гиф специально не используем: Compose не играет их без доп. библиотек,
> а APK раздувается. Лучший вариант — **статичная картинка в 2 фазы** (старт → финиш)
> со стрелкой направления движения, как на присланном тобой инфографике планки.
> Если очень нужна анимация — скажи, подключу Coil + animated WebP отдельно.

---

## МАСТЕР-ПРОМПТ (стиль — вставь один раз, подставляя предложение из таблицы)

```
Clean modern fitness instructional illustration, side view, a single athletic
male figure demonstrating: <SUBJECT>. Show TWO phases side by side (start position
and end position) with a subtle curved arrow indicating the movement direction.
Flat minimalist vector style, bold readable silhouette, thick clean lines.
Dark background #141925 with a violet #7C5CFF and gold #FFB454 accent on the
equipment and motion arrow. Correct anatomy and exercise form, neutral spine,
no text, no labels, no watermark, centered composition, square 1024x1024.
```

Подставляй `<SUBJECT>` из колонки «Что показать», имя файла бери из колонки «Файл».

| Файл | Упражнение | `<SUBJECT>` (что показать, с акцентом на технику) |
|---|---|---|
| `ex_pullup` | Подтягивания | pull-up on a bar, hanging with straight arms then chest pulled to the bar, shoulder blades down, elbows driving down |
| `ex_band_row` | Тяга гири в наклоне | one-arm bent-over kettlebell row, flat back hip-hinge, elbow driving back, kettlebell pulled to the waist |
| `ex_face_pull` | Face pull (резинка) | standing face pull with a resistance band at face height, elbows high, hands pulling apart toward the face |
| `ex_rear_delt_band` | Разведение резинки (задние дельты) | standing rear-delt band raise, straight arms pulling a band apart out to the sides, squeezing shoulder blades |
| `ex_dips` | Отжимания на брусьях | dips on parallel bars, lowering until elbows ~90°, slight forward lean, then pressing up |
| `ex_pushup` | Отжимания с упоров | push-up on push-up handles, straight rigid body, lowering chest between the handles, then pressing up |
| `ex_kb_press` | Жим гири стоя | standing one-arm kettlebell overhead press, kettlebell in the rack position then locked out overhead, tight core |
| `ex_db_lateral` | Махи гантелями в стороны | standing dumbbell lateral raise, arms by sides then raised out to shoulder height, leading with elbows |
| `ex_db_curl` | Сгибания на бицепс | standing dumbbell biceps curl, arms extended then curled up, elbows pinned to the sides |
| `ex_db_rear_fly` | Разведение гантелей в наклоне | bent-over dumbbell rear-delt fly, flat back, arms hanging then opened out to the sides |
| `ex_kb_swing` | Махи гирей | two-hand kettlebell swing, hip hinge with kettlebell between legs then snapped to chest height by hip drive |
| `ex_kb_goblet_squat` | Гоблет-приседания | goblet squat holding a kettlebell at the chest, standing then squatting to parallel, knees tracking toes |
| `ex_kb_clean` | Заброс гири | kettlebell clean, swing from between legs into the rack position at the shoulder |
| `ex_kb_snatch` | Рывок гири | one-arm kettlebell snatch, explosive pull from the floor to a locked-out overhead position |
| `ex_kb_tgu` | Турецкий подъём | Turkish get-up, lying on the floor then standing up while holding a kettlebell locked overhead |
| `ex_hanging_leg_raise` | Подъёмы ног в висе | hanging leg raise on a bar, hanging straight then raising straight legs to horizontal, no swinging |
| `ex_plank` | Планка | forearm plank, perfectly straight body line from heels to head, neutral spine |
| `ex_mobility_flow` | Мобилити-комплекс | dynamic mobility flow, cat-cow and thoracic rotation on a yoga mat |
| `ex_band_shoulder_dislocate` | Выкруты с резинкой | shoulder dislocates with a resistance band, straight arms passing a band overhead from front to back |
| `ex_hip_stretch` | Растяжка бёдер | deep low-lunge hip-flexor stretch on a mat, back knee down, chest tall |

---

## Если используешь Claude (claude.ai) с артефактами
Claude хорошо делает **SVG-иллюстрации**. Попроси его так:

```
Нарисуй SVG-иллюстрацию техники упражнения «<название>»: <SUBJECT из таблицы>.
Плоский минималистичный стиль, тёмный фон #141925, акцент фиолетовый #7C5CFF и
золотой #FFB454, две фазы движения со стрелкой, без текста, квадрат 1024x1024.
Выведи как один валидный SVG-файл.
```
Затем в Android Studio: `res → New → Vector Asset → Local file (SVG)` — импортирует
в Android-вектор (предупредит, если что-то не поддерживается). Либо экспортни SVG в
WebP/PNG 1024×1024 и положи как `ex_<id>.webp`.

## Если используешь генератор фото/реализма (Midjourney, DALL·E, SD)
Бери мастер-промпт как есть; добавь `--ar 1:1`. Скачай, переименуй в `ex_<id>.webp`,
положи в `drawable/`. Можно генерить по одному или пачкой.

---

Достаточно положить хотя бы часть файлов — остальные останутся силуэтами,
пока не добавишь. Приложение ничего не сломает, если файла нет.
