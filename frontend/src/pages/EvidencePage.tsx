import ComingSoonFeature from '../components/ComingSoonFeature'

export default function EvidencePage() {
  return (
    <ComingSoonFeature
      title="적격증빙자료 매칭"
      description="공모전/정부사업 공고문을 업로드하면 제출서류 목록을 추출하고 자료실 증빙자료와 자동 매칭해 ZIP으로 묶어드립니다."
      formHint="공고문 파일(docx/pdf/hwp) 업로드 — 증빙자료는 자료실에 미리 등록되어 있어야 합니다."
    />
  )
}
