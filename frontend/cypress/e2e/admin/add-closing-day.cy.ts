describe('Ajout jour de fermeture', () => {
  it('admin global ajoute un jour de fermeture sur un site', () => {
    cy.visit('http://localhost:4200/admin');

    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    cy.get('[data-cy="login-input"]')
      .type('AG01');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.contains(/administration/i)
      .click();

    cy.contains(/super admin/i)
      .should('be.visible');

    cy.get('[title="Sites"] > .block').click();

    cy.contains(/sites/i).should('be.visible');

    cy.get('button')
      .contains(/AJOUTER/i)
      .should('be.visible')
      .click();

    cy.get('.grid > :nth-child(1) > .w-full').type('2026-07-22');
    cy.get(':nth-child(2) > .w-full').type('Fete nationale');
    cy.get(':nth-child(3) > .w-full').select('Unique');

    cy.get('.fixed > .bg-white > .flex > .transition-all').click();
  });
});
