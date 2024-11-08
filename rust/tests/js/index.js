// Variables
let a = 10;
const b = 20;
var c = 30;

// Functions
function add(x, y) {
  return x + y;
}

// Arrow Functions
const subtract = (x, y) => x - y;

// Objects
const person = {
  firstName: 'John',
  lastName: 'Doe',
  age: 25,
};

// Arrays
const numbers = [1, 2, 3, 4, 5];

// Loops
for (let i = 0; i < numbers.length; i++) {
  console.log(numbers[i]);
}

numbers.forEach(num => console.log(num));

let i = 0;
while (i < numbers.length) {
  console.log(numbers[i]);
  i++;
}

// Conditionals
if (a > b) {
  console.log('a is greater than b');
} else if (a < b) {
  console.log('a is less than b');
} else {
  console.log('a is equal to b');
}

// Classes
class Animal {
  constructor(name) {
    this.name = name;
  }

  speak() {
    console.log(`${this.name} makes a noise.`);
  }
}

const dog = new Animal('Dog');
dog.speak();

// Promises
const promise = new Promise((resolve, reject) => {
  let success = true;
  if (success) {
    resolve('Promise resolved!');
  } else {
    reject('Promise rejected!');
  }
});

promise
  .then(message => {
    console.log(message);
  })
  .catch(error => {
    console.log(error);
  });

// Async/Await
async function fetchData() {
  try {
    let response = await fetch('https://api.example.com/data');
    let data = await response.json();
    console.log(data);
  } catch (error) {
    console.log(error);
  }
}

fetchData();

// Destructuring
const { firstName, lastName, age } = person;
console.log(`Name: ${firstName} ${lastName}, Age: ${age}`);

const [first, second, ...rest] = numbers;
console.log(`First: ${first}, Second: ${second}, Rest: ${rest}`);

// String Interpolation
const greeting = `Hello, my name is ${person.firstName} ${person.lastName} and I am ${person.age} years old.`;
console.log(greeting);
