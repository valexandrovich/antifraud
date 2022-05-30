const options = [
  "monday",
  "tuesday",
  "wednesday",
  "thursday",
  "friday",
  "saturday",
  "sunday",
];
const daysPeriodic = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
const month = [
  { name: "Січень", id: 1 },
  { name: "Лютий", id: 2 },
  { name: "Березень", id: 3 },
  { name: "Квітень", id: 4 },
  { name: "Травень", id: 5 },
  { name: "Червень", id: 6 },
  { name: "Липень", id: 7 },
  { name: "Серпень", id: 8 },
  { name: "Вересень", id: 9 },
  { name: "Жовтень", id: 10 },
  { name: "Листопад", id: 11 },
  { name: "Грудень", id: 12 },
];

const datOptions = [
  { name: "перший", value: 1 },
  { name: "другий", value: 2 },
  { name: "третій", value: 3 },
  { name: "четвертий", value: 4 },
  { name: "останній", value: -1 },
  { name: "передостанній", value: -2 },
];

const days = [
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
  23, 24, 25, 25, 26, 27, 28, -1, -2, -3,
];

const ua = {
  name: "ua",
  months: [
    ["Січень", "Січень"],
    ["Лютий", "Лютий"],
    ["Березень", "Березень"],
    ["Квітень", "Квітень"],
    ["Травень", "Травень"],
    ["Червень", "Червень"],
    ["Липень", "Липень"],
    ["Серпень", "Серпень"],
    ["Вересень", "Вересень"],
    ["Жовтень", "Жовтень"],
    ["Листопад", "Листопад"],
    ["Грудень", "Грудень"],
  ],
  weekDays: [
    ["Понеділок", "ПН"],
    ["Вівторок", "ВТ"],
    ["Суреда", "СР"],
    ["Четверг", "ЧТ"],
    ["П'ятниця", "ПТ"],
    ["Субота", "СБ"],
    ["Неділя", "НД"],
  ],
  digits: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"],
  meridiems: [
    ["AM", "am"],
    ["PM", "pm"],
  ],
};

const validate = (values, weekDays, time, period, minperiod) => {
  let errors = {};
  if (!values.groupName) {
    errors.groupName = "Назва групи обов'язкова для заповнення";
  }
  if (!values.name) {
    errors.name = "Шифр завдання обов'язковe для заповнення";
  }

  if (values.exchange.length === 0) {
    errors.exchange = "Назва черги сповіщень обов'язковe для заповнення";
  }
  if (values.schedule?.month) {
    if (
      weekDays.month &&
      period === "dayweek" &&
      Object.keys(weekDays.month) &&
      Object.keys(weekDays.month).length < 1
    ) {
      errors.days_of_week = "Потрібно обрати хоча-б один день тижня";
    }
  }

  if (
    time && minperiod === "periodic" && time.periodic === "0h0m"
  ) {
    errors.minutes_periodic = "Період повинен бути більше 1";
  }
  if (
    time &&
    time.once.type === "set" &&
    time.once.value.some((el) => /^([0-1]?[\d]|2[0-4]):([0-5][\d])(:[0-5][\d])?$/.test(el) !== true)
  ) {
    errors.minutes_set = "Невірний формат данних Введіть в форматі HH:mm";
  }
  return errors;
};
const scheduleSettings = {
  options,
  month,
  days,
  datOptions,
  validate,
  ua,
  daysPeriodic,
};

export default scheduleSettings;
