/**
 * Модуль для обробки параметрів пошуку та фільтрації на сторінці.
 *
 * Опис функціоналу:
 * ----------------
 * 1. **searchFields** - масив, який містить інформацію про поля пошуку. За замовчуванням порожній.
 *    Якщо на сторінці є пошукові поля, просто перевизначте змінну.
 *
 * 2. **getSearchParams(fields)** - отримує параметри пошуку з DOM на основі переданого масиву searchFields.
 *
 * 3. **getCustomSearchParams()** - отримує параметри пошуку без додаткової логіки. Якщо потрібно додати додаткову логіку - перевизначте функцію.
 *
 * 4. **buildSearchUrl()** - будує URL для пошуку з скиданням номера сторінки.
 *
 * 5. **updateURL(params)** - оновлює URL із заданими параметрами.
 *
 * 6. **updateFilter(selectElement, paramName, isNumeric = false)** - оновлює параметр фільтрації та перенаправляє з оновленими параметрами.
 *
 * 7. **updateParamsAndRedirect(params, keepPage = false)** - оновлює параметри URL і перенаправляє на нову адресу.
 *
 * 8. **updatePageSize(selectElement)** - оновлює розмір сторінки, зберігаючи параметри пошуку.
 *
 * 9. **updateSort(selectElement)** - оновлює параметр сортування, зберігаючи параметри пошуку.
 *
 * 10. **goToPage(pageNumber, event)** - здійснює перехід на задану сторінку (пагінація).
 *
 * 11. **handleEnterKeyForSearch(fields, callback)** - обробляє натискання клавіші Enter у вказаних полях.
 *
 * Приклад використання:
 * ---------------------
 * 1. Ініціалізація поля пошуку:
 *    ```javascript
 *    searchFields = [
 *        { id: 'searchInput', param: 'query' },
 *        { id: 'categorySelect', param: 'category' }
 *    ];
 *    ```
 *
 * 2. Виклик функції з додатковою логікою:
 *    ```javascript
 *    function getCustomSearchParams() {
 *         const params = getSearchParams(searchFields);
 *         if (params.rentalId && parseInt(params.rentalId) <= 0) {
 *             delete params.rentalId;  // Remove invalid rentalId
 *         }
 *         return params;
 *     }
 *    ```
 *
 * 3. Використання фільтру для кастомних фільтрів:
 *    ```javascript
 *        function updateFilterByRentalStatus(selectElement) {
 *         updateFilter(selectElement, 'statusId', true);
 *     }
 *    ```
 *
 *    4. Додавання можливості пошуку при натисканні Enter:
 *    ```javascript
 *     const searchInputs = document.querySelectorAll('#rentalIdSearch, #carPlateNumberSearch, #clientLastNameSearch');
 *     handleEnterKeyForSearch(searchInputs);
 *    ```
 *
 * Детальніше можна ознайомитись з документацією.
 */


// Порожній масив для зберігання полів пошуку
let searchFields = [];

// Функція для отримання параметрів пошуку з DOM на основі переданого масиву
function getSearchParams(fields) {
    const params = new URLSearchParams(); // Порожній об'єкт

    // Перебираємо всі поля і додаємо значення до параметрів
    if (Array.isArray(fields) && fields.length > 0) {
        fields.forEach(field => {
            const element = document.getElementById(field.id);
            if (element) {
                const value = element.value.trim();
                if (value) {
                    params.set(field.param, value); // Додаємо параметр
                }
            }
        });
    }

    // Якщо є елемент для розміру сторінки, беремо його значення
    const pageSizeElement = document.getElementById('pageSizeSelect');
    if (pageSizeElement) {
        const pageSize = pageSizeElement.value;
        if (pageSize) {
            params.set('pageSize', pageSize);
        }
    }

    return params;
}

// Функція для отримання параметрів пошуку з DOM без додаткової логіки
function getCustomSearchParams() {
    return getSearchParams(searchFields);
}

// Функція для побудови URL для пошуку з скиданням номера сторінки
function buildSearchUrl() {
    const params = getCustomSearchParams();
    applyUrlParamsAndRedirect(params); // Оновлюємо URL
}

// Функція для оновлення URL з параметрами
function updateURL(params) {
    const url = new URL(window.location); // Беремо поточний URL
    url.search = params.toString(); // Оновлюємо параметри запиту
    return url;
}

/**
 * Оновлює параметр фільтрації та перенаправляє з оновленими параметрами
 * @param {string} paramName - ім'я параметра URL
 * @param {string} value - значення параметра
 * @param {boolean} [isNumeric=false] - чи є значення числовим (для додаткової валідації)
 */
// Універсальна функція для оновлення параметрів в URL
function updateUrlParam(paramName, value, isNumeric = false) {
    const params = getSearchParams(searchFields);

    value && (!isNumeric || parseInt(value) > 0)
        ? params.set(paramName, value)
        : params.delete(paramName)

    applyUrlParamsAndRedirect(params);
}

// Оновлення параметра фільтру
function updateFilter(selectElement, paramName, isNumeric = false) {
    const value = selectElement.value;
    updateUrlParam(paramName, value, isNumeric);
}

// Оновлення розміру сторінки
function updatePageSize(selectElement) {
    const pageSize = selectElement.value;
    updateUrlParam('pageSize', pageSize);
}

// Оновлення сортування
function updateSort(selectElement) {
    const sortBy = selectElement.value;
    updateUrlParam('sortBy', sortBy);
}

// Перехід на задану сторінку (пагінація)
function goToPage(pageNumber, event) {
    if (event) event.preventDefault();
    const params = getCustomSearchParams();
    params.set('page', pageNumber);
    applyUrlParamsAndRedirect(params, true);
}

// Функція для оновлення параметрів URL і редиректу
function applyUrlParamsAndRedirect(params, keepPage = false) {
    // Скидаємо номер сторінки, якщо він є, якщо keepPage = false
    if (!keepPage) {
        params.delete('page');
    }
    window.location.href = updateURL(params).toString(); // Оновлюємо сторінку з новими параметрами
}

// Функція для обробки натискання клавіші Enter у вказаних полях
function handleEnterKeyForSearch(fields) {
    // Перетворюємо NodeList в масив, якщо потрібно
    Array.from(fields).forEach(input => {
        input.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                buildSearchUrl();
            }
        });
    });
}

function clearFilters() {
    const params = new URLSearchParams();
    applyUrlParamsAndRedirect(params);
}