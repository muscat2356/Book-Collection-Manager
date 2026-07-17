import { Navigate, Outlet, useLocation, useMatch } from "react-router-dom"
import { useLoanCheckout } from "../loans/LoanContext"
import { CheckoutSelectionSummary } from "./CheckoutSelectionSummary"

export function CheckoutLayout() {
  const { user } = useLoanCheckout()
  const location = useLocation()
  const isBookDetail = Boolean(useMatch("/loans/checkout/books/:id"))

  const isUserSelect = location.pathname === "/loans/checkout"
  const isConfirm = location.pathname === "/loans/checkout/confirm"

  if (!user && !isUserSelect) {
    return <Navigate to="/loans/checkout" replace />
  }

  if (isUserSelect || isConfirm) {
    return (
      <div className="page">
        <Outlet />
      </div>
    )
  }

  return (
    <div className="page checkout-layout checkout-layout--with-summary">
      <header className="checkout-header">
        <h1>{isBookDetail ? "貸出 — 所蔵を選ぶ" : "貸出 — 本を選ぶ"}</h1>
        <p className="checkout-header__user">利用者: {user?.displayName}</p>
      </header>
      <div className="checkout-layout__main">
        <Outlet />
      </div>
      <aside className="checkout-layout__aside">
        <CheckoutSelectionSummary />
      </aside>
    </div>
  )
}
