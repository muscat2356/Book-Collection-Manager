import { Link, useParams } from "react-router-dom"
import type { Book, BookHolding, CopyStatus } from "../types/Book";
import { useApiClient } from "../api/ApiClientContext";
import { useLoanCheckout } from "../loans/LoanContext";
import { useEffect, useState } from "react";
import { fetchBookById } from "../api/books";

function statusLabel(status: CopyStatus): string {
    return status === "AVAILABLE" ? "貸出可" : "貸出中"
}

export function CheckoutBookDetailPage() {
    const { id } = useParams()
    const apiClient = useApiClient()
    const { selectedBookCopies, addCopy, removeCopy } = useLoanCheckout()

    const [book, setBook] = useState<Book | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)    

    useEffect(() => {
        if (!id) {
          setError("書籍IDが指定されていません")
          setLoading(false)
          return
        }
        let ignore = false
        setLoading(true)
        setError(null)
        fetchBookById(apiClient, id)
          .then((data) => {
            if (ignore) return
            if (!data) {
              setError("書籍が見つかりません")
              setBook(null)
              return
            }
            setBook(data)
          })
          .catch((err) => {
            if (!ignore) setError(err.message)
          })
          .finally(() => {
            if (!ignore) setLoading(false)
          })
        return () => {
          ignore = true
        }
      }, [id, apiClient])


      if (loading) return <p className="page-status">読み込み中…</p>
      if (error) return <p className="page-status page-status--error">{error}</p>
      if (!book) return <p className="page-status page-status--error">書籍が見つかりません</p>    
      
      const holdings = book.holdings ?? []
      const alreadySelectedForThisBook = selectedBookCopies.some(
        (c) => c.bookId === book.id
      )

      function onToggle(holding: BookHolding, checked: boolean) {
        if(!book || holding.status !== "AVAILABLE") return

        if(checked) {
          if(selectedBookCopies.some(
            (c) => c.bookId === book.id && c.bookCopyId !== holding.id
          )
        ) 
        {
          return
        }

        addCopy({bookCopyId: holding.id, bookId:book.id, title: book.title,})
        }
        else{
          removeCopy(holding.id)
        }
      }

      return (
        <div>
        <Link to="/loans/checkout/books" className="back-link">
          ← 一覧へ
        </Link>
        <h2>{book.title}</h2>
        <p>
          <strong>著者：</strong>
          {book.author}
        </p>
        <p>
          <strong>ISBN：</strong>
          {book.isbn}
        </p>

        <section className="holdings">
            <h3 className="holdings__title">所蔵</h3>
            <p className="page__lead">同じ書籍は1冊まで選択できます。</p>
            {holdings.length === 0 ? (
                <p className="holdings__empty">所蔵がありません</p>
            ) : (
                <ul className="checkout-holding-list">
                    {holdings.map((h) => {
                        const checked = selectedBookCopies.some(
                            (c) => c.bookCopyId === h.id
                        )
                        const disabled = h.status === "LOANED" || (alreadySelectedForThisBook && !checked)
                        return (
                            <li key={h.id}>
                            <label
                              className={`checkout-holding${
                                disabled ? " checkout-holding--disabled" : ""
                              }`}
                            >
                              <input
                                type="checkbox"
                                checked={checked}
                                disabled={disabled}
                                onChange={(e) => onToggle(h, e.target.checked)}
                              />
                              <span>
                                #{h.id}{" "}
                                <span
                                  className={`holdings__status holdings__status--${h.status.toLowerCase()}`}
                                >
                                  {statusLabel(h.status)}
                                </span>
                              </span>
                            </label>
                          </li>
                        )
                    })}
                </ul>
            )}
        </section>
        </div>
      )
}
