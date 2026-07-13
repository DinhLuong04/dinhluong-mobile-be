package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.AdminReplyRequest;
import com.dinhluong.dlmstore.dto.responses.AdminCommentResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductComment;
import com.dinhluong.dlmstore.entity.ProductCommentImage;
import com.dinhluong.dlmstore.repository.ProductCommentRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ProductCommentRepository productCommentRepository;
    private final ProductRepository productRepository;
    @Transactional(readOnly = true)
    public Page<AdminCommentResponse> getAdminReviews(String keyword, ProductComment.CommentStatus status, Pageable pageable) {
        Page<ProductComment> parentComments = productCommentRepository.searchAdminComments(keyword, status, pageable);
        
        List<Long> parentIds = parentComments.getContent().stream()
                .map(ProductComment::getId)
                .collect(Collectors.toList());

        List<ProductComment> allReplies = new ArrayList<>();
        if (!parentIds.isEmpty()) {
            allReplies = productCommentRepository.findByParentIdInOrderByCreatedAtAsc(parentIds);
        }

        Map<Long, List<ProductComment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(ProductComment::getParentId));

        return parentComments.map(comment -> mapToResponse(comment, repliesMap.getOrDefault(comment.getId(), new ArrayList<>())));
    }

    @Transactional
    public void updateReviewStatus(Long id, ProductComment.CommentStatus status) {
        ProductComment comment = productCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận!"));
        comment.setStatus(status);
        productCommentRepository.save(comment);
    }

    @Transactional
    public void replyToReview(Long parentId, AdminReplyRequest request) {
        ProductComment parentComment = productCommentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận gốc!"));

        ProductComment reply = ProductComment.builder()
                .productId(parentComment.getProductId())
                .parentId(parentComment.getId())
                .authorName("Quản trị viên") // Hoặc lấy tên admin đang đăng nhập
                .content(request.getContent())
                .isAdminReply(true)
                .status(ProductComment.CommentStatus.APPROVED) // Reply của admin thì auto duyệt
                .build();

        productCommentRepository.save(reply);
    }

    @Transactional
    public void deleteReview(Long id) {
        ProductComment comment = productCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận!"));
        productCommentRepository.delete(comment);
    }

    private AdminCommentResponse mapToResponse(ProductComment comment, List<ProductComment> replies) {
        List<String> imageUrls = new ArrayList<>();
        if (comment.getImages() != null) {
            imageUrls = comment.getImages().stream()
                    .map(ProductCommentImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        List<AdminCommentResponse> replyResponses = replies.stream()
                .map(reply -> mapToResponse(reply, new ArrayList<>()))
                .collect(Collectors.toList());
        Product product = productRepository.findById(comment.getProductId()).orElse(null);
        return AdminCommentResponse.builder()
                .id(comment.getId())
                .productId(comment.getProductId())
                .productName(product != null ? product.getName() : "Sản phẩm không tồn tại")
                .productThumbnail(product != null ? product.getThumbnailUrl() : null)
                .productSlug(product != null ? product.getSlug() : null)
                .userId(comment.getUserId())
                .authorName(comment.getAuthorName())
                .authorPhone(comment.getAuthorPhone())
                .authorAvatar(comment.getAuthorAvatar())
                .rating(comment.getRating())
                .content(comment.getContent())
                .isPurchased(comment.getIsPurchased())
                .isAdminReply(comment.getIsAdminReply())
                .status(comment.getStatus() != null ? comment.getStatus().name() : null)
                .createdAt(comment.getCreatedAt())
                .imageUrls(imageUrls)
                .replies(replyResponses)
                .build();
    }
}