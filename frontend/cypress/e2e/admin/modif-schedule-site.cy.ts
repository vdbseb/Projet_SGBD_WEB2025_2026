describe('Modification horaires site', () => {
  it('admin modifie les horaires dun site', () => {
    cy.visit('http://localhost:4200/admin');

    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    cy.get('[data-cy="login-input"]')
      .type('AG01');

    cy.get('[data-cy="admin-password-input"]')
      .type('admin123');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.visit('http://localhost:4200/admin');

    cy.contains(/super admin/i)
      .should('be.visible');

    cy.get('[title="Sites"] > .block')
      .click();

    cy.contains(/sites/i)
      .should('be.visible');

    cy.get('button')
      .contains(/MODIFIER/i)
      .should('be.visible')
      .click();

    cy.get('.grid > :nth-child(1) > .w-full')
      .clear()
      .type('12:00');

    cy.get('.grid > :nth-child(2) > .w-full')
      .clear()
      .type('23:00');

    cy.get('button')
      .contains(/ENREGISTRER/i)
      .should('be.visible')
      .click();
  });
});
