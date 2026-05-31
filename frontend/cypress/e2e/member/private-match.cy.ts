describe('Création match privé', () => {

  it('un membre crée un match privé', () => {

    cy.visit('http://localhost:4200');

    cy.get('[data-cy="header-login-button"]').click();

    cy.get('[data-cy="login-input"]').type('S0005');

    cy.get('[data-cy="login-button"]').click();

    cy.get(':nth-child(4) > .mat-mdc-card > .mat-mdc-card-actions > [data-cy="select-court-button"] > .mdc-button__label').click();

    cy.get(':nth-child(1) > .mat-mdc-card > .mat-mdc-card-actions > [data-cy="select-court-button"] > .mdc-button__label').click();

    cy.contains('match privé').should('be.visible');

    cy.get('.date-strip-container > :nth-child(6)').click();

    cy.get('[data-cy="time-slot"]').click();

    cy.get(':nth-child(6) > .w-full').type('L0003', {delay:100});
    cy.get(':nth-child(7) > .w-full').type('L0005', {delay:100});
    cy.get(':nth-child(8) > .w-full').type('L0004', {delay:100});

    cy.get('[data-cy="confirm-booking-button"] > .mdc-button__label').click();
  });
});
