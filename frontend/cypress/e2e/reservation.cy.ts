describe('Reservation happy flow', () => {

  it('should create a public reservation successfully', () => {

    cy.visit('http://localhost:4200');

    // Choix du centre
    cy.contains(/réserve à bruxelles/i)
      .click();

    // Login membre
    cy.get('[data-cy="login-input"]')
      .type('G0001');

    cy.get('[data-cy="login-button"]')
      .click();

    // Vérification page centre
    cy.contains(/the atomium padel club/i)
      .should('be.visible');

    // Choix terrain
    cy.get('[data-cy="select-court-button"]')
      .first()
      .click();

    // Choix créneau
    cy.get('[data-cy="time-slot"]')
      .contains('08:00')
      .click();

// TYPE DE MATCH
    cy.get('[data-cy="public-open-match"]')
      .click();

// Confirmation réservation
    cy.get('[data-cy="confirm-booking-button"]')
      .click();

    // Vérification succès
    cy.contains(/réservation confirmée/i)
      .should('be.visible');

  });

});
