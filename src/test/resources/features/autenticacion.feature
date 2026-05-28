Feature: Autenticación de usuarios

  Scenario: Registro exitoso de una inmobiliaria
    Given que no existe una cuenta con el email "nueva@inmo.com"
    When una inmobiliaria se registra con nombre "Quilmes Prop", email "nueva@inmo.com" y contraseña "segura123"
    Then la respuesta debería contener un token JWT
    And el email del token debería ser "nueva@inmo.com"

  Scenario: Login con credenciales incorrectas
    Given que existe un usuario con email "user@test.com" y contraseña "123456"
    When intenta hacer login con contraseña "erronea"
    Then debería recibir un error de autenticación

  Scenario: Registro duplicado
    Given que existe un usuario con email "dup@test.com" y contraseña "123456"
    When una inmobiliaria se registra con nombre "Dup", email "dup@test.com" y contraseña "123456"
    Then debería recibir un error de conflicto
    And el código de error debería ser "CONFLICT"

  Scenario: Login con credenciales correctas
    Given que existe un usuario con email "ok@test.com" y contraseña "123456"
    When intenta hacer login con contraseña "123456"
    Then la respuesta debería contener un token JWT
    And el email del token debería ser "ok@test.com"
