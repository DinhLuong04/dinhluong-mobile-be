package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.*;
import com.dinhluong.dlmstore.dto.responses.ReviewResponse;
import com.dinhluong.dlmstore.entity.ProductComment.CommentStatus;
import com.dinhluong.dlmstore.entity.ProductComment;
import com.dinhluong.dlmstore.entity.ProductCommentImage;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.repository.OrderRepository;
import com.dinhluong.dlmstore.repository.ProductCommentRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;
import com.dinhluong.dlmstore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ProductCommentRepository commentRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    // ==========================================
    // 1. GET: LẤY DANH SÁCH ĐÁNH GIÁ (THEO SLUG)
    // ==========================================
    public ReviewResponse getProductReviewsBySlug(String productSlug, Integer rating, int page, int limit, Long currentUserId) {

        Product product = productRepository.findBySlug(productSlug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với slug: " + productSlug));

        Long productId = product.getId();

        // Xử lý lọc theo số sao
        Page<ProductComment> pagedComments;
        if (rating != null) {
            pagedComments = commentRepository.findByProductIdAndParentIdIsNullAndStatusAndRatingOrderByCreatedAtDesc(
                    productId, CommentStatus.APPROVED, rating, PageRequest.of(page - 1, limit));
        } else {
            pagedComments = commentRepository.findByProductIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
                    productId, CommentStatus.APPROVED, PageRequest.of(page - 1, limit));
        }

        List<ProductComment> baseComments = pagedComments.getContent();

        List<Long> baseCommentIds = baseComments.stream().map(ProductComment::getId).collect(Collectors.toList());
        List<ProductComment> allReplies = baseCommentIds.isEmpty() ? new ArrayList<>()
                : commentRepository.findByParentIdInAndStatusOrderByCreatedAtAsc(baseCommentIds, CommentStatus.APPROVED);

        Map<Long, List<ProductComment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(ProductComment::getParentId));

        List<ReviewCommentDTO> commentDTOs = baseComments.stream().map(c -> {
            boolean isMine = currentUserId != null && currentUserId.equals(c.getUserId());

            List<ReviewMediaDTO> mediaDTOs = c.getImages() != null ? c.getImages().stream()
                    .map(img -> ReviewMediaDTO.builder()
                            .id(img.getId())
                            .image_url(img.getImageUrl())
                            .is_video(img.getIsVideo())
                            .build())
                    .collect(Collectors.toList()) : new ArrayList<>();

            List<ProductComment> replies = repliesMap.getOrDefault(c.getId(), new ArrayList<>());
            List<ReviewReplyDTO> replyDTOs = replies.stream()
                    .map(r -> {
                        boolean isReplyMine = currentUserId != null && currentUserId.equals(r.getUserId());
                        return ReviewReplyDTO.builder()
                                .id(r.getId())
                                .author_name(r.getAuthorName())
                                .author_avatar(r.getAuthorAvatar())
                                .is_admin_reply(r.getIsAdminReply())
                                .content(r.getContent())
                                .created_at(r.getCreatedAt().toString())
                                .is_mine(isReplyMine) 
                                .build();
                    })
                    .collect(Collectors.toList());

            return ReviewCommentDTO.builder()
                    .id(c.getId())
                    .author_name(c.getAuthorName())
                    .author_avatar(c.getAuthorAvatar())
                    .rating(c.getRating() != null ? c.getRating() : 0)
                    .content(c.getContent())
                    .created_at(c.getCreatedAt().toString())
                    .is_mine(isMine)
                    .is_purchased(c.getIsPurchased() != null ? c.getIsPurchased() : false)
                    .images(mediaDTOs)
                    .replies(replyDTOs)
                    .build();
        }).collect(Collectors.toList());

        List<Object[]> ratingsData = commentRepository.countRatingsByProductId(productId);
        long totalReviews = 0;
        long totalStars = 0;
        Map<Integer, Long> countByStar = new HashMap<>();
        for (int i = 1; i <= 5; i++) countByStar.put(i, 0L);

        for (Object[] row : ratingsData) {
            if (row[0] == null) continue;
            Integer star = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            countByStar.put(star, count);
            totalReviews += count;
            totalStars += (star * count);
        }

        double average = totalReviews == 0 ? 0 : (double) totalStars / totalReviews;
        average = Math.round(average * 10.0) / 10.0;

        List<BreakdownItemDTO> breakdownItems = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            long count = countByStar.get(i);
            double percent = totalReviews == 0 ? 0 : ((double) count / totalReviews) * 100;
            breakdownItems.add(BreakdownItemDTO.builder()
                    .star(i)
                    .count(count)
                    .percent(Math.round(percent * 10.0) / 10.0)
                    .build());
        }

        // TÍNH NĂNG MỚI: Check xem user đang đăng nhập có được quyền đánh giá không
        boolean currentUserPurchased = false;
        if (currentUserId != null) {
            try {
                currentUserPurchased = orderRepository.hasUserPurchasedProduct(currentUserId, productId);
            } catch (Exception e) {
                // Ignore hoặc log
            }
        }

        ReviewSummaryDTO summaryDTO = ReviewSummaryDTO.builder()
                .average(average)
                .totalCount(totalReviews)
                .breakdown(breakdownItems)
                .currentUserHasPurchased(currentUserPurchased) // Gửi xuống React
                .build();

        return ReviewResponse.builder()
                .summary(summaryDTO)
                .comments(commentDTOs)
                .build();
    }

    // ==========================================
    // 2. POST: GỬI ĐÁNH GIÁ (THEO SLUG)
    // ==========================================
    public void submitReviewBySlug(String productSlug, Integer rating, String content, Long userId,
            List<MultipartFile> files, Long parentId) {

        Product product = productRepository.findBySlug(productSlug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với slug: " + productSlug));

        Long productId = product.getId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // TÍNH NĂNG MỚI: Check đơn hàng để cấp huy hiệu "Đã mua hàng" cho bình luận
        boolean isPurchased = false;
        try {
            isPurchased = orderRepository.hasUserPurchasedProduct(userId, productId);
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra mua hàng: " + e.getMessage());
        }

        // 1. Tạo đối tượng Comment
        ProductComment newComment = ProductComment.builder()
                .productId(productId)
                .userId(userId)
                .parentId(parentId)
                .authorName(user.getFullName()) 
                .authorAvatar(user.getAvatarUrl())
                .rating(rating)
                .content(content)
                .isPurchased(isPurchased) // Lưu vào Database ở đây
                .status(CommentStatus.APPROVED)
                .build();

        // 2. Xử lý Upload file đa luồng
        if (files != null && !files.isEmpty()) {
            List<CompletableFuture<ProductCommentImage>> uploadFutures = files.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String uploadedUrl = cloudinaryService.uploadFile(file);
                            boolean isVideo = file.getContentType() != null && file.getContentType().startsWith("video");
                            return ProductCommentImage.builder()
                                    .comment(newComment)
                                    .imageUrl(uploadedUrl)
                                    .isVideo(isVideo)
                                    .build();
                        } catch (Exception e) {
                            throw new RuntimeException("Lỗi tải file: " + file.getOriginalFilename(), e);
                        }
                    }))
                    .collect(Collectors.toList());

            List<ProductCommentImage> uploadedImages = uploadFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            newComment.setImages(uploadedImages);
        }

        // 3. Lưu Database
        commentRepository.save(newComment);
    }
}