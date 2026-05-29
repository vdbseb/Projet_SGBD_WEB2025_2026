describe('Public match flow', () => {

  it('should join a public match successfully', () => {

    cy.visit('http://localhost:4200');


    cy.get('[data-cy="header-login-button"]')
      .click();


    cy.get('[data-cy="login-input"]')
      .type('L0002');

    cy.get('[data-cy="login-button"]')
      .click();


    cy.contains(/matchs publics/i)
      .click();


    cy.contains(/match public/i)
      .should('be.visible');


    cy.contains('A1')
      .closest('.bg-white')
      .within(() => {
        cy.get('[data-cy="join-public-match-button"]').click();
      });


    cy.contains(/payer 15€/i)
      .click();


    cy.contains(/vous avez rejoint le match/i)
      .should('be.visible');

  });

});
