import type { BookCategoryPath } from "../types/Book"

type BookCategoryProps = {
  category: BookCategoryPath | null
}

/**
 * カテゴリの表示のみ（色分けは data-category-large + CSS）
 */
export function BookCategory({ category }: BookCategoryProps) {
  if (!category) {
    return (
      <div className="book-category book-category--empty" data-category-large="">
        <span className="book-category__empty">未分類</span>
      </div>
    )
  }

  return (
    <div
      className="book-category"
      data-category-large={category.largeName}
    >
      <span className="book-category__chip book-category__chip--large">
        {category.largeName}
      </span>
      <span className="book-category__sep" aria-hidden="true">
        ›
      </span>
      <span className="book-category__chip book-category__chip--medium">
        {category.mediumName}
      </span>
      <span className="book-category__sep" aria-hidden="true">
        ›
      </span>
      <span className="book-category__chip book-category__chip--small">
        {category.smallName}
      </span>
    </div>
  )
}
