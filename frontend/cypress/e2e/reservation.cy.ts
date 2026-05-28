describe('Reservation happy flow', () => {

  it('should create a public reservation successfully', () => {

    cy.visit('http://localhost:4200');


    cy.contains(/réserve à liège/i)
      .click();


    cy.get('[data-cy="login-input"]')
      .type('G0001');

    cy.get('[data-cy="login-button"]')
      .click();


    cy.contains(/the carré club/i)
      .should('be.visible');


    cy.get('[data-cy="select-court-button"]')
      .first()
      .click();


    cy.get('[data-cy="time-slot"]')
      .contains('09:00')
      .click();


    cy.get('[data-cy="public-open-match"]')
      .click();


    cy.get('[data-cy="confirm-booking-button"]')
      .click();


    cy.contains(/réservation confirmée/i)
      .should('be.visible');

  });

});
