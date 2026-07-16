import { Link } from "react-router-dom"
import { useLoanCheckout } from "../loans/LoanContext"

export function CheckoutSelectionSummary() {
  const { selectedBookCopies, removeCopy, clearSelection } = useLoanCheckout()
  const count = selectedBookCopies.length

  return (
    <section className="checkout-summary">
      <div className="checkout-summary-header">
        <h2>選択中 ({count})</h2>
        {count > 0 && (
          <button type="button" className="btn" onClick={clearSelection}>
            すべて解除
          </button>
        )}
      </div>

      {count === 0 ? (
        <p className="page__lead">所蔵を選ぶとここに表示されます</p>
      ) : (
        <ul className="checkout-summary__list">
          {selectedBookCopies.map((c) => (
            <li key={c.bookCopyId} className="checkout-summary__item">
              <span>
                {c.title}
                <br />
                <small>所蔵 #{c.bookCopyId}</small>
              </span>
              <button
                type="button"
                aria-label={`${c.title} 所蔵 ${c.bookCopyId} を削除`}
                onClick={() => removeCopy(c.bookCopyId)}
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="checkout-summary__actions">
        <button
          type="button"
          className="btn"
          onClick={clearSelection}
          disabled={count === 0}
        >
          選択を解除
        </button>
        <Link
          to="/loans/checkout/confirm"
          className="btn btn--primary"
          onClick={(e) => {
            if (count === 0) e.preventDefault()
          }}
          aria-disabled={count === 0}
        >
          確認へ →
        </Link>
      </div>
    </section>
  )
}
