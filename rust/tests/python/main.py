n = 10

if n < 0:
    result = "Negative"
elif n == 0:
    result = "Zero"
else:
    result = "Positive"

# Traditional for loop
for i in range(5):
    a = i * 2
    print(f"Iteration {i}: {a}")

# Traditional while loop
count = 0
while count < 5:
    print(f"Count is {count}")
    count += 1
