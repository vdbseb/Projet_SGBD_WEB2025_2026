describe('Public match flow', () => {

  it('should join a public match successfully', () => {

    cy.visit('http://localhost:4200');

    // Ouvrir login via header
    cy.get('[data-cy="header-login-button"]')
      .click();

    // Login membre
    cy.get('[data-cy="login-input"]')
      .type('L0002');

    cy.get('[data-cy="login-button"]')
      .click();

    // Aller vers Matchs publics
    cy.contains(/matchs publics/i)
      .click();

    // Vérification page
    cy.contains(/match public/i)
      .should('be.visible');

    // Choisir le match B1
    cy.contains('L1')
      .closest('.bg-white')
      .within(() => {
        cy.get('[data-cy="join-public-match-button"]').click();
      });

    // Confirmation paiement
    cy.contains(/payer 15€/i)
      .click();

    // Succès
    cy.contains(/vous avez rejoint le match/i)
      .should('be.visible');

  });

});
