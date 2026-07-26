import ComingSoonFeature from '../components/ComingSoonFeature'

export default function DocumentPage() {
  return (
    <ComingSoonFeature
      title="문서 자동 채우기"
      description="Word(.docx) 양식을 선택하면 회사 고정정보는 자료실에서 자동으로 채우고, 이번 건에만 해당하는 항목만 입력받습니다."
      formHint="템플릿 선택 후 남은 항목만 입력 — 회사명/사업자번호 등은 자동으로 채워집니다."
    />
  )
}
