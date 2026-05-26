Feature: Gestión de publicaciones

  Scenario: Una inmobiliaria crea una publicación
    Given que estoy autenticado como inmobiliaria
    When publico un departamento en "Av. Mitre 123" piso "2" depto "A"
    Then la publicación debería aparecer en los resultados de búsqueda

  Scenario: Una inmobiliaria no puede editar la publicación de otra
    Given que existe una publicación de "Quilmes Prop"
    When "Otra Inmobiliaria" intenta modificarla
    Then debería recibir un error de permisos