describe('Closing days flow', () => {

  it('should block reservation on the day after 18', () => {

    cy.visit('http://localhost:4200');

    cy.contains(/réserve à bruxelles/i)
      .click();

    cy.get('[data-cy="login-input"]')
      .type('G0001');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.contains(/the atomium padel club/i)
      .should('be.visible');

    cy.get('[data-cy="select-court-button"]')
      .first()
      .click();

    cy.contains('button', /18/)
      .scrollIntoView()
      .should('be.visible');

    cy.get('button')
      .then(($buttons) => {
        const buttons = [...$buttons];

        const index18 = buttons.findIndex((button) =>
          button.textContent?.includes('18')
        );

        expect(index18).to.be.greaterThan(-1);

        cy.wrap(buttons[index18 + 1])
          .scrollIntoView()
          .should('be.visible')
          .and('be.disabled')
          .and('contain.text', '19')
          .and('contain.text', 'Fermé');
      });

  });

});
