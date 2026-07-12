/** Mock dataset for responsive UI demos */
export const DEMO_ITEMS = [
  {
    id: 1001,
    name: '노트북 Pro 14',
    category: '전자제품',
    status: 'ACTIVE',
    price: 1890000,
    stock: 24,
    owner: '김민수',
    updatedAt: '2026-07-01',
    description: '가벼운 업무용 노트북. 반응형 레이아웃 테스트용 샘플 데이터입니다.',
  },
  {
    id: 1002,
    name: '무선 키보드',
    category: '주변기기',
    status: 'ACTIVE',
    price: 89000,
    stock: 120,
    owner: '이서연',
    updatedAt: '2026-07-03',
    description: '저소음 멤브레인 키보드.',
  },
  {
    id: 1003,
    name: '모니터 27인치',
    category: '전자제품',
    status: 'INACTIVE',
    price: 320000,
    stock: 8,
    owner: '박준호',
    updatedAt: '2026-06-28',
    description: 'QHD IPS 패널 모니터.',
  },
  {
    id: 1004,
    name: 'USB-C 허브',
    category: '주변기기',
    status: 'ACTIVE',
    price: 45000,
    stock: 56,
    owner: '최유진',
    updatedAt: '2026-07-05',
    description: 'HDMI / SD / USB3 멀티 허브.',
  },
  {
    id: 1005,
    name: '스탠딩 데스크',
    category: '가구',
    status: 'ACTIVE',
    price: 450000,
    stock: 15,
    owner: '정하늘',
    updatedAt: '2026-07-08',
    description: '높이 조절형 스탠딩 데스크.',
  },
  {
    id: 1006,
    name: '인체공학 의자',
    category: '가구',
    status: 'PENDING',
    price: 280000,
    stock: 3,
    owner: '오세린',
    updatedAt: '2026-07-09',
    description: '요추 지지 메쉬 의자.',
  },
]

export const CATEGORIES = ['전자제품', '주변기기', '가구']
export const STATUSES = [
  { value: 'ACTIVE', label: '판매중', type: 'success' },
  { value: 'INACTIVE', label: '판매중지', type: 'info' },
  { value: 'PENDING', label: '검수중', type: 'warning' },
]

export function findDemoItem(id) {
  const num = Number(id)
  return DEMO_ITEMS.find((item) => item.id === num) || null
}

export function statusMeta(status) {
  return STATUSES.find((s) => s.value === status) || { value: status, label: status, type: 'info' }
}

export function formatPrice(value) {
  return new Intl.NumberFormat('ko-KR').format(value) + '원'
}
