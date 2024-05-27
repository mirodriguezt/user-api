# Spring boot example with REST and spring data JPA 

User control API

### Running
Run docker
- Execute docker-compose up
- To review swagger access: -> http://localhost:8090/swagger-ui/index.html

### Endpoints

| Method | Url | Decription |
| ------ | --- | ---------- |
| PUT    |/user/{id} | Modify a user record given its id |
| DELETE |/user/{id} | Delete a user given their id |
| GET    |/user/username/{username} | Get a user by username |
| PUT    |/user/username/{username} | Modify a user given their username |
| DELETE |/user/username/{username} | Delete a user given their username |
| GET    |/user/cpf/{cpf} | Get a user by cpf |
| PUT    |/user/cpf/{cpf} | Modify a user given their cpf |
| DELETE |/user/cpf/{cpf} | Delete a user given their cpf |
| POST   |/user/add | Add a user |
| GET    |/user/user/filter/lastname | Gets users given last name |
| GET    |/user/user/filter/firstname | Gets users given first name |
| GET    |/user/all | Get all users |
