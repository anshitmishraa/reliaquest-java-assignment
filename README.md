# Coding Challenge 

### In this assessment you will be tasked with filling out the functionality of different methods that will be listed further down. 
These methods will require some level of api interactions with the following base url: https://dummy.restapiexample.com.
Please keep the following in mind when doing this assessment: clean coding practices, test driven development, logging, and scalability.
If you are unable to successfully receive responses from the endpoints, mocking the response calls may prove to be helpful.

Endpoints to implement

getAllEmployees()
output - list of employees
description - this should return all employees

getEmployeesByNameSearch()
output - list of employees
description - this should return all employees whose name contains or matches the string input provided

getEmployeeById(string id)
output - employee
description - this should return a single employee

getHighestSalaryOfEmployees()
output - integer of the highest salary
description -  this should return a single integer indicating the highest salary of all employees

getTop10HighestEarningEmployeeNames()
output - list of employees
description -  this should return a list of the top 10 employees based off of their salaries

createEmployee(string name, string salary, string age)
output - string of the status (i.e. success)
description -  this should return a status of success or failed based on if an employee was created



deleteEmployee(String id)
output - the name of the employee that was deleted
description - this should delete the employee with specified id given
